package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.interpret.RecordsetTransformation;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.List;
import java.util.Objects;

/**
 * Table pattern (Def. 28): the root of the ATP hierarchy.
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

    /** Convenience: table pattern from subtable patterns, no transformations. */
    public static TablePattern of(SubtablePattern... subtables) {
        return new TablePattern(List.of(subtables), List.of());
    }

    /** Returns a copy of this pattern with the given post-processing transformations attached. */
    public TablePattern withTransformations(RecordsetTransformation... transforms) {
        return new TablePattern(subtablePatterns, List.of(transforms));
    }

    /** Applies all transformations in order to the given recordset. */
    public Recordset transform(Recordset rs) {
        for (var t : transformations) rs = t.apply(rs);
        return rs;
    }
}
