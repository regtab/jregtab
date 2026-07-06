package ru.icc.regtab.rtl;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.interpret.DelimitedFieldSplit;
import ru.icc.regtab.interpret.RecordsetTransformation;
import ru.icc.regtab.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serializes an ATP {@link TablePattern} into an RTL string.
 *
 * <p>Limitations:
 * <ul>
 *   <li>Actions are emitted at atom level (after {@code :}) — inherited-level action specs
 *       are not reconstructed from the merged {@link AtomicContentSpec#actions()} list.</li>
 *   <li>{@link CellPredicate.Custom} and {@link ItemFilterConditionSpec.Custom} throw
 *       {@link UnsupportedOperationException} — use only for tasks without custom predicates.
 *       Externally bound predicates ({@link CellPredicate.External}, {@link FilterTerm.External})
 *       serialize to {@code EXT('name')}; compiling the output back requires the same
 *       {@link Bindings}.</li>
 * </ul>
 */
public final class AtpToRtlSerializer {

    private AtpToRtlSerializer() {}

    // -------- entry point --------

    public static String serialize(TablePattern pattern) {
        String settings = serializeSettings(pattern.transformations());
        String cond = pattern.condition() != null
                ? serializeCellMatchConstr(pattern.condition()) + "? "
                : "";
        List<SubtablePattern> subtables = pattern.subtablePatterns();
        String body;
        if (subtables.size() == 1 && isImplicitSubtable(subtables.get(0))) {
            body = serializeImplicitSubtable(subtables.get(0));
        } else {
            body = subtables.stream()
                    .map(AtpToRtlSerializer::serializeExplicitSubtable)
                    .collect(Collectors.joining(" "));
        }
        return settings + cond + body;
    }

    private static String serializeSettings(List<RecordsetTransformation> transformations) {
        if (transformations.isEmpty()) return "";
        String inner = transformations.stream()
                .map(AtpToRtlSerializer::serializeSetting)
                .collect(Collectors.joining(", "));
        return "<" + inner + "> ";
    }

    private static String serializeSetting(RecordsetTransformation t) {
        return switch (t) {
            case AnchorAttributeAtPosition a -> "ANCH(" + a.position() + ")";
            case WhitespaceNormalization()       -> "NORM";
            case DelimitedFieldSplit d       -> "SPLIT(\"" + escapeString(d.delimiter()) + "\")";
            default -> throw new UnsupportedOperationException("Cannot serialize transformation: " + t);
        };
    }

    // -------- subtable --------

    private static boolean isImplicitSubtable(SubtablePattern sp) {
        return sp.condition() == null && sp.quantifier().kind() == Quantifier.Kind.ONE;
    }

    private static String serializeImplicitSubtable(SubtablePattern sp) {
        return sp.rowPatterns().stream()
                .map(AtpToRtlSerializer::serializeRow)
                .collect(Collectors.joining(" "));
    }

    private static String serializeExplicitSubtable(SubtablePattern sp) {
        StringBuilder sb = new StringBuilder("{ ");
        if (sp.condition() != null) sb.append(serializeCellMatchConstr(sp.condition())).append("? ");
        sp.rowPatterns().forEach(rp -> sb.append(serializeRow(rp)).append(" "));
        sb.append("}").append(serializeQuantifier(sp.quantifier()));
        return sb.toString();
    }

    // -------- row --------

    private static String serializeRow(RowPattern rp) {
        StringBuilder sb = new StringBuilder("[ ");
        if (rp.condition() != null) sb.append(serializeCellMatchConstr(rp.condition())).append("? ");
        rp.subrowPatterns().forEach(sr -> sb.append(serializeSubrow(sr)).append(" "));
        sb.append("]").append(serializeQuantifier(rp.quantifier()));
        return sb.toString();
    }

    // -------- subrow --------

    private static String serializeSubrow(SubrowPattern sr) {
        if (sr.condition() == null && sr.quantifier().kind() == Quantifier.Kind.ONE) {
            return sr.cellPatterns().stream()
                    .map(AtpToRtlSerializer::serializeCell)
                    .collect(Collectors.joining(" "));
        }
        StringBuilder sb = new StringBuilder("{ ");
        if (sr.condition() != null) sb.append(serializeCellMatchConstr(sr.condition())).append("? ");
        sr.cellPatterns().forEach(cp -> sb.append(serializeCell(cp)).append(" "));
        sb.append("}").append(serializeQuantifier(sr.quantifier()));
        return sb.toString();
    }

    // -------- cell --------

    private static String serializeCell(CellPattern cp) {
        StringBuilder sb = new StringBuilder("[");
        boolean hasBody = cp.condition() != null || cp.contentSpec() != null;
        if (hasBody) {
            sb.append(" ");
            if (cp.condition() != null) {
                sb.append(serializeCellMatchConstr(cp.condition()));
                if (cp.contentSpec() != null) sb.append("? ");
                else sb.append(" ");
            }
            if (cp.contentSpec() != null) sb.append(serializeContentSpec(cp.contentSpec())).append(" ");
        }
        sb.append("]").append(serializeQuantifier(cp.quantifier()));
        return sb.toString();
    }

    // -------- content spec --------

    private static String serializeContentSpec(ContentSpec cs) {
        return switch (cs) {
            case AtomicContentSpec a      -> serializeAtomic(a);
            case DelimitedContentSpec d   -> serializeDelimited(d);
            case CompoundContentSpec c    -> serializeCompound(c);
            case ConditionalContentSpec c -> serializeConditional(c);
        };
    }

    private static String serializeAtomic(AtomicContentSpec a) {
        StringBuilder sb = new StringBuilder(serializeIdd(a.idd()));
        if (!a.tags().isEmpty()) {
            String tagStr = a.tags().stream()
                    .map(t -> "#'" + escapeSQ(t.startsWith("#") ? t.substring(1) : t) + "'")
                    .collect(Collectors.joining(" "));
            sb.append(" ").append(tagStr);
        }
        if (a.extractor() != null && !(a.extractor() instanceof StringExtractor.Verbatim)) {
            sb.append(" = ").append(a.extractor().toRtl());
        }
        if (!a.actions().isEmpty()) {
            sb.append(" : ").append(serializeActSpecs(a.actions()));
        }
        return sb.toString();
    }

    private static String serializeDelimited(DelimitedContentSpec d) {
        return "(" + serializeAtomic(d.atomicSpec()) + "){\"" + escapeString(d.delimiter()) + "\"}";
    }

    private static String serializeCompound(CompoundContentSpec c) {
        StringBuilder sb = new StringBuilder();
        List<CompoundSegment> segs = c.segments();
        // first segment: openDelim (if non-empty) + spec
        String openDelim = segs.get(0).leadingDelimiter();
        if (!openDelim.isEmpty()) sb.append("\"").append(escapeString(openDelim)).append("\" ");
        sb.append(serializeSegSpec(segs.get(0).spec()));
        // remaining segments: separator + spec
        for (int i = 1; i < segs.size(); i++) {
            sb.append(" \"").append(escapeString(segs.get(i).leadingDelimiter())).append("\" ");
            sb.append(serializeSegSpec(segs.get(i).spec()));
        }
        // trailing delimiter
        if (!c.trailingDelimiter().isEmpty()) {
            sb.append(" \"").append(escapeString(c.trailingDelimiter())).append("\"");
        }
        return sb.toString();
    }

    private static String serializeSegSpec(ContentSpec cs) {
        return switch (cs) {
            case AtomicContentSpec a    -> serializeAtomic(a);
            case DelimitedContentSpec d -> serializeDelimited(d);
            default -> throw new UnsupportedOperationException(
                    "Unsupported compound segment type: " + cs.getClass().getSimpleName());
        };
    }

    private static String serializeConditional(ConditionalContentSpec c) {
        return serializeCellMatchConstr(c.condition()) + "? "
                + serializeXContSpec(c.positive())
                + " | " + serializeXContSpec(c.negative());
    }

    private static String serializeXContSpec(ContentSpec cs) {
        return switch (cs) {
            case AtomicContentSpec a      -> serializeAtomic(a);
            case DelimitedContentSpec d   -> serializeDelimited(d);
            case CompoundContentSpec c    -> serializeCompound(c);
            default -> throw new UnsupportedOperationException(
                    "Unsupported xContSpec type: " + cs.getClass().getSimpleName());
        };
    }

    // -------- action specs --------

    private static String serializeActSpecs(List<ActionSpec> actions) {
        return actions.stream()
                .map(AtpToRtlSerializer::serializeActSpec)
                .collect(Collectors.joining(", "));
    }

    private static String serializeActSpec(ActionSpec as) {
        return serializeProvSpecs(as.providers()) + "->" + serializeOp(as);
    }

    private static String serializeProvSpecs(List<ProviderSpec> providers) {
        if (providers.size() == 1) return serializeProvSpec(providers.get(0));
        return "(" + providers.stream()
                .map(AtpToRtlSerializer::serializeProvSpec)
                .collect(Collectors.joining(", ")) + ")";
    }

    private static String serializeProvSpec(ProviderSpec ps) {
        if (ps.isContextLiteral()) {
            if (ps.contextLiteral().constValue() != null) {
                return "@'" + escapeSQ(ps.contextLiteral().text()) + "'='"
                        + escapeSQ(ps.contextLiteral().constValue()) + "'";
            }
            return "'" + escapeSQ(ps.contextLiteral().text()) + "'";
        }
        String order = serializeTraversalOrder(ps.traversalOrder());
        String cond  = ps.filterCondition().toRtl();
        String card  = serializeCardinality(ps.cardinality());
        return order + cond + card;
    }

    private static String serializeOp(ActionSpec as) {
        return switch (as.operationType()) {
            case AVP    -> "AVP";
            case REC    -> {
                if (as.anchorPos()      != null) yield "REC(" + as.anchorPos() + ")";
                if (as.splitDelimiter() != null) yield "REC('" + escapeString(as.splitDelimiter()) + "')";
                yield "REC";
            }
            case JOIN -> {
                Set<Integer> kp = as.keyPositions();
                if (kp.isEmpty()) yield "JOIN";
                String args = kp.stream().sorted().map(Object::toString).collect(Collectors.joining(", "));
                yield "JOIN(" + args + ")";
            }
            case FILL   -> as.delimiter().isEmpty() ? "FILL" : "FILL(\"" + escapeString(as.delimiter()) + "\")";
            case PREFIX -> as.delimiter().isEmpty() ? "PREFIX" : "PREFIX(\"" + escapeString(as.delimiter()) + "\")";
            case SUFFIX -> as.delimiter().isEmpty() ? "SUFFIX" : "SUFFIX(\"" + escapeString(as.delimiter()) + "\")";
        };
    }

    // -------- cell match condition --------

    private static String serializeCellMatchConstr(CellMatchCondition cmc) {
        return cmc.cellPredicate().toRtl();
    }

    // -------- helpers --------

    private static String serializeIdd(ItemDerivationDirective idd) {
        return switch (idd) {
            case VAL  -> "VAL";
            case ATTR -> "ATTR";
            case AUX  -> "AUX";
            case SKIP -> "SKIP";
        };
    }

    private static String serializeQuantifier(Quantifier q) {
        return switch (q.kind()) {
            case ONE          -> "";
            case ZERO_OR_ONE  -> "?";
            case ONE_OR_MORE  -> "+";
            case ZERO_OR_MORE -> "*";
            case EXACTLY      -> "{" + q.n() + "}";
        };
    }

    private static String serializeTraversalOrder(TraversalOrder order) {
        return switch (order) {
            case ROW_MAJOR          -> "";
            case REVERSE_ROW_MAJOR  -> "-";
            case COLUMN_MAJOR       -> "^";
            case REVERSE_COLUMN_MAJOR -> "-^";
        };
    }

    private static String serializeCardinality(int cardinality) {
        if (cardinality == 1)                   return "";
        if (cardinality == ProviderSpec.UNBOUNDED) return "*";
        return "{" + cardinality + "}";
    }

    /** Escapes embedded double-quote characters by doubling them (for use in "..." strings). */
    private static String escapeString(String s) {
        return s.replace("\"", "\"\"");
    }

    /** Escapes embedded single-quote characters by doubling them (for use in '...' strings). */
    private static String escapeSQ(String s) {
        return s.replace("'", "''");
    }
}
