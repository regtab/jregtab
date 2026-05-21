package ru.icc.regtab.itm.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Row pattern (def:rp):
 * P_row = (λ, q, ⟨P_sr¹, …, P_srᵏ⟩), k ≥ 1.
 *
 * @param label      optional label for pattern reuse (null if absent; not in formal def:rp)
 * @param condition  optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param subrowPatterns ordered sequence of subrow patterns P_sr (≥ 1)
 */
public record RowPattern(
        String label,
        CellMatchCondition condition,
        Quantifier quantifier,
        List<SubrowPattern> subrowPatterns
) {
    public RowPattern {
        if (quantifier == null) quantifier = Quantifier.one();
        subrowPatterns = List.copyOf(Objects.requireNonNull(subrowPatterns, "subrowPatterns"));
        if (subrowPatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one subrow pattern is required");
        }
    }

    /** Convenience: row with one subrow containing the given cell patterns. */
    public static RowPattern of(CellPattern... cells) {
        return new RowPattern(null, null, Quantifier.one(),
                List.of(SubrowPattern.of(cells)));
    }

    /** Convenience: row with quantifier and one subrow. */
    public static RowPattern of(Quantifier q, CellPattern... cells) {
        return new RowPattern(null, null, q,
                List.of(SubrowPattern.of(cells)));
    }

    /** Convenience: row with explicit subrow patterns. */
    public static RowPattern of(Quantifier q, SubrowPattern... subrows) {
        return new RowPattern(null, null, q, List.of(subrows));
    }

    /** Convenience: row with condition, quantifier, and one subrow. */
    public static RowPattern of(CellMatchCondition cond, Quantifier q, CellPattern... cells) {
        return new RowPattern(null, cond, q,
                List.of(SubrowPattern.of(cells)));
    }
}
