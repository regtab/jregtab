package ru.icc.regtab.rtl.internal;

import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemDerivationDirective;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;
import ru.icc.regtab.rtl.RTLParser;
import ru.icc.regtab.rtl.RtlCompileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves RTL provider specifications into {@link ProviderSpec}.
 *
 * <p>Each {@code tblProvSpec} has an optional traversal-order mark and spatial/content constraints:
 * <ul>
 *   <li>(no mark) → ROW_MAJOR</li>
 *   <li>{@code -}  → REVERSE_ROW_MAJOR</li>
 *   <li>{@code ^}  → COLUMN_MAJOR</li>
 *   <li>{@code -^} → REVERSE_COLUMN_MAJOR</li>
 * </ul>
 *
 * <p>Named spatial constraints and their base filter conditions:
 * <ul>
 *   <li>LT: sameSubrow(a) &amp;&amp; col &lt; col(a)</li>
 *   <li>RT: sameSubrow(a) &amp;&amp; col &gt; col(a)</li>
 *   <li>AV: sameSubcol(a) &amp;&amp; row &lt; row(a)</li>
 *   <li>BW: sameSubcol(a) &amp;&amp; row &gt; row(a)</li>
 *   <li>ROW: sameRow(a) &amp;&amp; !sameCell(a)</li>
 *   <li>COL: sameCol(a) &amp;&amp; !sameCell(a)</li>
 *   <li>SR: sameSubrow(a) &amp;&amp; !sameCell(a)</li>
 *   <li>SC: sameSubcol(a) &amp;&amp; !sameCell(a)</li>
 *   <li>ST: sameSubtable(a) &amp;&amp; !sameCell(a)</li>
 *   <li>TAB: !sameCell(a)</li>
 *   <li>CL: sameCell(a)</li>
 * </ul>
 * Additional col/row/pos and content constraints are AND-ed with the named base condition.
 */
final class ProviderTemplateResolver {

    private ProviderTemplateResolver() {}

    /**
     * Builds a {@link ProviderSpec} from an RTL {@code tblProvSpec} context.
     * The provider kind is inferred from the action type and anchor item type.
     */
    static ProviderSpec resolve(RTLParser.TblProvSpecContext ctx,
                                RTLParser.OpContext op,
                                ItemDerivationDirective anchorType) {
        TraversalOrder order = parseTraversalOrder(ctx.traversalOrderMark());
        int cardinality = parseCardinality(ctx.cardinality());
        ItemFilterConditionSpec condition = buildCondition(ctx);
        CellDerivedProviderKind kind = inferKind(op, anchorType);
        int actualCardinality = (kind == CellDerivedProviderKind.ATTR) ? 1 : cardinality;
        return new ProviderSpec(actualCardinality, order, condition, kind, null);
    }

    // --- Traversal order ---

    private static TraversalOrder parseTraversalOrder(RTLParser.TraversalOrderMarkContext ctx) {
        if (ctx == null)                          return TraversalOrder.ROW_MAJOR;
        if (ctx.columnMajor()        != null)     return TraversalOrder.COLUMN_MAJOR;
        if (ctx.reverseRowMajor()    != null)     return TraversalOrder.REVERSE_ROW_MAJOR;
        if (ctx.reverseColumnMajor() != null)     return TraversalOrder.REVERSE_COLUMN_MAJOR;
        throw new RtlCompileException("Unknown traversal order mark");
    }

    // --- Kind inference ---

    private static CellDerivedProviderKind inferKind(RTLParser.OpContext op,
                                                     ItemDerivationDirective anchorType) {
        if (op == null) return CellDerivedProviderKind.UNRESTRICTED;
        if (op.recOp() != null || op.CONCAT() != null) return CellDerivedProviderKind.VAL;
        if (op.AVP() != null)                        return CellDerivedProviderKind.ATTR;
        return CellDerivedProviderKind.UNRESTRICTED;
    }

    // --- Cardinality ---

    private static int parseCardinality(RTLParser.CardinalityContext ctx) {
        if (ctx == null)              return 1;
        if (ctx.MULT() != null)       return ProviderSpec.UNBOUNDED;
        return Integer.parseInt(ctx.INT().getText());
    }

    // --- Condition building ---

    private static ItemFilterConditionSpec buildCondition(RTLParser.TblProvSpecContext ctx) {
        if (ctx.spatConstr() != null) {
            List<FilterTerm> parts = new ArrayList<>();
            addSpatConstrParts(ctx.spatConstr(), parts);
            return toSpec(parts);
        }
        if (ctx.constraints() != null) return buildConstraints(ctx.constraints());
        return ItemFilterConditionSpec.bare(new FilterTerm.SameCell());
    }

    private static ItemFilterConditionSpec buildConstraints(RTLParser.ConstraintsContext ctx) {
        List<RTLParser.OrGroupContext> orGroups = ctx.orGroup();
        if (orGroups.size() == 1) return buildOrGroup(orGroups.get(0));
        List<ItemFilterConditionSpec.And> allAnds = new ArrayList<>();
        for (RTLParser.OrGroupContext g : orGroups) {
            ItemFilterConditionSpec spec = buildOrGroup(g);
            switch (spec) {
                case ItemFilterConditionSpec.Bare(var c) ->
                        allAnds.add(new ItemFilterConditionSpec.And(List.of(c)));
                case ItemFilterConditionSpec.And a        -> allAnds.add(a);
                case ItemFilterConditionSpec.Or(var gs)   -> allAnds.addAll(gs);
                default -> throw new RtlCompileException("Unexpected spec type in OR");
            }
        }
        return new ItemFilterConditionSpec.Or(allAnds);
    }

    /**
     * Expands an orGroup (possibly containing nested parenthesized ORs) into a list of
     * And-groups by distributing nested ORs: {@code A & (B|C)} → {@code (A&B)|(A&C)}.
     */
    private static ItemFilterConditionSpec buildOrGroup(RTLParser.OrGroupContext ctx) {
        List<List<FilterTerm>> distributed = expandOrGroup(ctx);
        if (distributed.size() == 1) return toSpec(distributed.get(0));
        return new ItemFilterConditionSpec.Or(
                distributed.stream().map(ProviderTemplateResolver::toAndSpec).toList());
    }

    private static List<List<FilterTerm>> expandOrGroup(RTLParser.OrGroupContext ctx) {
        List<List<FilterTerm>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (RTLParser.BaseConstrContext bc : ctx.baseConstr()) {
            List<List<FilterTerm>> alternatives = expandBaseConstr(bc);
            List<List<FilterTerm>> next = new ArrayList<>();
            for (List<FilterTerm> existing : result) {
                for (List<FilterTerm> alt : alternatives) {
                    List<FilterTerm> combined = new ArrayList<>(existing);
                    combined.addAll(alt);
                    next.add(combined);
                }
            }
            result = next;
        }
        return result;
    }

    private static List<List<FilterTerm>> expandBaseConstr(RTLParser.BaseConstrContext ctx) {
        if (ctx.constraints() != null) {
            ItemFilterConditionSpec inner = buildConstraints(ctx.constraints());
            return switch (inner) {
                case ItemFilterConditionSpec.Bare(var c)  -> List.of(List.of(c));
                case ItemFilterConditionSpec.And(var ts)  -> List.of(new ArrayList<>(ts));
                case ItemFilterConditionSpec.Or(var gs)   -> gs.stream()
                        .map(g -> (List<FilterTerm>) new ArrayList<>(g.terms()))
                        .toList();
                default -> throw new RtlCompileException("Unexpected nested spec type");
            };
        }
        return List.of(buildConstrConstraints(ctx.constr()));
    }

    private static List<FilterTerm> buildConstrConstraints(RTLParser.ConstrContext ctx) {
        List<FilterTerm> parts = new ArrayList<>();
        if (ctx.spatConstr() != null) addSpatConstrParts(ctx.spatConstr(), parts);
        else if (ctx.contConstr() != null) parts.add(buildContentConstraint(ctx.contConstr()));
        return parts;
    }

    private static void addSpatConstrParts(RTLParser.SpatConstrContext ctx,
                                           List<FilterTerm> parts) {
        FilterTerm named = namedSpatConstraint(ctx);
        if (named != null) parts.add(named);
        if (ctx.col() != null) parts.add(colConstraint(ctx.col()));
        if (ctx.row() != null) parts.add(rowConstraint(ctx.row()));
        if (ctx.pos() != null) parts.add(posConstraint(ctx.pos()));
    }

    private static FilterTerm namedSpatConstraint(RTLParser.SpatConstrContext ctx) {
        if (ctx.LEFT_OF()   != null) return FilterTerm.LeftOf.INSTANCE;
        if (ctx.RIGHT_OF()  != null) return FilterTerm.RightOf.INSTANCE;
        if (ctx.ABOVE()     != null) return FilterTerm.Above.INSTANCE;
        if (ctx.BELOW()     != null) return FilterTerm.Below.INSTANCE;
        if (ctx.SAME_ROW()       != null) return FilterTerm.SameRow.INSTANCE;
        if (ctx.SAME_COLUMN()    != null) return FilterTerm.SameCol.INSTANCE;
        if (ctx.SAME_SUBROW()    != null) return FilterTerm.SameSubrow.INSTANCE;
        if (ctx.SAME_SUBCOLUMN() != null) return FilterTerm.SameSubcol.INSTANCE;
        if (ctx.SAME_SUBTABLE()  != null) return FilterTerm.SameSubtable.INSTANCE;
        if (ctx.NOT_SAME_CELL()  != null) return FilterTerm.NotSameCell.INSTANCE;
        if (ctx.SAME_CELL()      != null) return FilterTerm.SameCell.INSTANCE;
        return null; // col/row/pos — no named base condition
    }

    // --- Spatial constraint builders ---

    private static FilterTerm colConstraint(RTLParser.ColContext ctx) {
        if (ctx.range() != null) return colRangeConstraint(ctx.range());
        if (ctx.offset() != null) return new FilterTerm.ColOffset(parseOffset(ctx.offset()));
        return new FilterTerm.ColExact(Integer.parseInt(ctx.INT().getText()));
    }

    private static FilterTerm colRangeConstraint(RTLParser.RangeContext ctx) {
        boolean hiOpen = ctx.end() == null;
        boolean startIsOffset = ctx.start().offset() != null;
        int lo = boundaryValue(ctx.start(), 0);
        int hi = hiOpen ? Integer.MAX_VALUE : boundaryValue(ctx.end(), Integer.MAX_VALUE);
        // Offset start → relative range (C+n..); INT start → absolute range (Cn..m)
        return startIsOffset ? new FilterTerm.ColRange(lo, hi) : new FilterTerm.ColAbsoluteRange(lo, hi);
    }

    private static FilterTerm rowConstraint(RTLParser.RowContext ctx) {
        if (ctx.range() != null) {
            int lo = boundaryValue(ctx.range().start(), 0);
            if (ctx.range().start().offset() != null) return new FilterTerm.RowOffset(lo);
            return new FilterTerm.RowExact(lo);
        }
        if (ctx.offset() != null) return new FilterTerm.RowOffset(parseOffset(ctx.offset()));
        return new FilterTerm.RowExact(Integer.parseInt(ctx.INT().getText()));
    }

    private static FilterTerm posConstraint(RTLParser.PosContext ctx) {
        if (ctx.range()  != null) {
            int lo = boundaryValue(ctx.range().start(), 0);
            int hi = ctx.range().end() == null ? Integer.MAX_VALUE : boundaryValue(ctx.range().end(), Integer.MAX_VALUE);
            return new FilterTerm.PosRange(lo, hi);
        }
        if (ctx.offset() != null) return new FilterTerm.PosOffset(parseOffset(ctx.offset()));
        return new FilterTerm.PosExact(Integer.parseInt(ctx.INT().getText()));
    }

    // --- Content constraint builders ---

    private static FilterTerm buildContentConstraint(RTLParser.ContConstrContext ctx) {
        if (ctx.regex()    != null) return regexConstraint(ctx.regex());
        if (ctx.blank()    != null) return blankConstraint(ctx.blank());
        if (ctx.tag()      != null) return tagConstraint(ctx.tag());
        if (ctx.sameStr()  != null) return FilterTerm.SameStr.INSTANCE;
        if (ctx.contains() != null) return containsConstraint(ctx.contains());
        throw new RtlCompileException("Unknown content constraint");
    }

    private static FilterTerm regexConstraint(RTLParser.RegexContext ctx) {
        String pattern = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        return ctx.EXCLAMATION() != null
                ? new FilterTerm.NotRegexMatched(pattern)
                : new FilterTerm.RegexMatched(pattern);
    }

    private static FilterTerm blankConstraint(RTLParser.BlankContext ctx) {
        return ctx.EXCLAMATION() != null ? FilterTerm.NotBlank.INSTANCE : FilterTerm.Blank.INSTANCE;
    }

    private static FilterTerm tagConstraint(RTLParser.TagContext ctx) {
        List<String> tags = ctx.tagItem().stream()
                .map(t -> "#" + StringExtractorFactory.parseStringLiteral(t.STRING().getText()))
                .toList();
        return ctx.EXCLAMATION() != null
                ? new FilterTerm.NotTagged(tags)
                : new FilterTerm.Tagged(tags);
    }

    private static FilterTerm containsConstraint(RTLParser.ContainsContext ctx) {
        String substring = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        return ctx.EXCLAMATION() != null
                ? new FilterTerm.NotContains(substring)
                : new FilterTerm.Contains(substring);
    }

    // --- Helper: list of constraints → ItemFilterConditionSpec ---

    private static ItemFilterConditionSpec toSpec(List<FilterTerm> parts) {
        if (parts.size() == 1) return new ItemFilterConditionSpec.Bare(parts.get(0));
        return new ItemFilterConditionSpec.And(List.copyOf(parts));
    }

    private static ItemFilterConditionSpec.And toAndSpec(List<FilterTerm> parts) {
        return new ItemFilterConditionSpec.And(List.copyOf(parts));
    }

    // --- Numeric helpers ---

    private static int parseOffset(RTLParser.OffsetContext ctx) {
        int n = Integer.parseInt(ctx.INT().getText());
        return ctx.MINUS() != null ? -n : n;
    }

    private static int boundaryValue(RTLParser.StartContext ctx, int defaultVal) {
        if (ctx.offset() != null) return parseOffset(ctx.offset());
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        return defaultVal;
    }

    private static int boundaryValue(RTLParser.EndContext ctx, int defaultVal) {
        if (ctx.offset() != null) return parseOffset(ctx.offset());
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        return defaultVal;
    }
}
