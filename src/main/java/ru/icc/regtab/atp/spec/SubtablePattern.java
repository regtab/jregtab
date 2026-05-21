package ru.icc.regtab.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Subtable pattern (def:stp):
 * P_st = (λ, q, ⟨P_row¹, …, P_rowᵏ⟩), k ≥ 1.
 *
 * @param condition  optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param rowPatterns ordered sequence of row patterns P_row (≥ 1)
 */
public record SubtablePattern(
        CellMatchCondition condition,
        Quantifier quantifier,
        List<RowPattern> rowPatterns
) {
    public SubtablePattern {
        if (quantifier == null) quantifier = Quantifier.one();
        rowPatterns = List.copyOf(Objects.requireNonNull(rowPatterns, "rowPatterns"));
        if (rowPatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one row pattern is required");
        }
    }

    /** Convenience: subtable with given row patterns and quantifier. */
    public static SubtablePattern of(Quantifier q, RowPattern... rows) {
        return new SubtablePattern(null, q, List.of(rows));
    }

    /** Convenience: single subtable. */
    public static SubtablePattern of(RowPattern... rows) {
        return new SubtablePattern(null, Quantifier.one(), List.of(rows));
    }
}
