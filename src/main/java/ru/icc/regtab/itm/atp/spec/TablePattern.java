package ru.icc.regtab.itm.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Table pattern (Def. 28): the root of the ATP hierarchy.
 * P_tbl = ⟨P_st¹, …, P_stᵏ⟩, k ≥ 1.
 *
 * @param subtablePatterns ordered sequence of subtable patterns (≥ 1)
 */
public record TablePattern(
        List<SubtablePattern> subtablePatterns
) {
    public TablePattern {
        subtablePatterns = List.copyOf(Objects.requireNonNull(subtablePatterns, "subtablePatterns"));
        if (subtablePatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one subtable pattern is required");
        }
    }

    /** Convenience: table pattern from subtable patterns. */
    public static TablePattern of(SubtablePattern... subtables) {
        return new TablePattern(List.of(subtables));
    }
}
