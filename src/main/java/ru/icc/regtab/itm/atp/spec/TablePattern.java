package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.interpret.RecordsetTransformation;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Table pattern (def:atp): the root of the ATP hierarchy.
 * P_tbl = ⟨P_st¹, …, P_stᵏ⟩, k ≥ 1.
 *
 * @param subtablePatterns ordered sequence of subtable patterns (≥ 1)
 * @param transformations  optional post-processing transformations applied after interpretation
 */
public record TablePattern(
        List<SubtablePattern> subtablePatterns,
        List<RecordsetTransformation> transformations
) {
    public TablePattern {
        subtablePatterns = List.copyOf(Objects.requireNonNull(subtablePatterns, "subtablePatterns"));
        transformations  = List.copyOf(Objects.requireNonNull(transformations,  "transformations"));
        if (subtablePatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one subtable pattern is required");
        }
    }

    /**
     * Convenience: table pattern from subtable patterns.
     * Inline REC params ({@link ActionSpec#anchorPos()}, {@link ActionSpec#splitDelimiter()})
     * found anywhere in the pattern are automatically converted to transformations.
     */
    public static TablePattern of(SubtablePattern... subtables) {
        List<SubtablePattern> list = List.of(subtables);
        return new TablePattern(list, extractInlineTransformations(list));
    }

    /** Returns a copy of this pattern with the given post-processing transformations attached. */
    public TablePattern withTransformations(RecordsetTransformation... transforms) {
        return new TablePattern(subtablePatterns, List.of(transforms));
    }

    /** Applies all transformations in order to the given recordset. */
    public Recordset transform(Recordset rs) {
        for (var t : transformations)
            rs = t.withAnonymousAttributeTemplate("$a_%i").apply(rs);
        return rs;
    }

    // --- Inline param extraction ---

    private static List<RecordsetTransformation> extractInlineTransformations(List<SubtablePattern> subtables) {
        Integer anchorPos = null;
        String splitDelimiter = null;
        for (var st : subtables) {
            for (var row : st.rowPatterns()) {
                for (var subrow : row.subrowPatterns()) {
                    for (var cell : subrow.cellPatterns()) {
                        ContentSpec cs = cell.contentSpec();
                        if (cs == null) continue;
                        for (var a : actionsOf(cs)) {
                            if (a.anchorPos() != null) {
                                if (anchorPos != null && !anchorPos.equals(a.anchorPos()))
                                    throw new IllegalArgumentException(
                                            "Conflicting inline anchorPos: " + anchorPos + " vs " + a.anchorPos());
                                anchorPos = a.anchorPos();
                            }
                            if (a.splitDelimiter() != null) {
                                if (splitDelimiter != null && !splitDelimiter.equals(a.splitDelimiter()))
                                    throw new IllegalArgumentException(
                                            "Conflicting inline splitDelimiter: \"" + splitDelimiter + "\" vs \"" + a.splitDelimiter() + "\"");
                                splitDelimiter = a.splitDelimiter();
                            }
                        }
                    }
                }
            }
        }
        List<RecordsetTransformation> result = new ArrayList<>();
        if (anchorPos != null)      result.add(new AnchorAttributeAtPosition(anchorPos));
        if (splitDelimiter != null) result.add(new DelimitedFieldSplit(splitDelimiter));
        return List.copyOf(result);
    }

    private static List<ActionSpec> actionsOf(ContentSpec cs) {
        return switch (cs) {
            case AtomicContentSpec a         -> a.actions();
            case DelimitedContentSpec d      -> d.atomicSpec().actions();
            case ConditionalContentSpec cond -> {
                var result = new ArrayList<ActionSpec>();
                result.addAll(actionsOf(cond.positive()));
                result.addAll(actionsOf(cond.negative()));
                yield result;
            }
            case CompoundContentSpec comp    -> {
                var result = new ArrayList<ActionSpec>();
                for (var seg : comp.segments()) result.addAll(actionsOf(seg.spec()));
                yield result;
            }
        };
    }
}
