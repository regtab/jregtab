package ru.icc.regtab.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Subrow pattern (def:srp):
 * P_sr = (λ, q, ⟨P_cell¹, …, P_cellᵏ⟩), k ≥ 1.
 *
 * @param condition  optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param cellPatterns ordered sequence of cell patterns P_cell (≥ 1)
 */
public record SubrowPattern(
        CellMatchCondition condition,
        Quantifier quantifier,
        List<CellPattern> cellPatterns
) {
    public SubrowPattern {
        if (quantifier == null) quantifier = Quantifier.one();
        cellPatterns = List.copyOf(Objects.requireNonNull(cellPatterns, "cellPatterns"));
        if (cellPatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one cell pattern is required");
        }
    }

    /** Convenience: single subrow with given cell patterns. */
    public static SubrowPattern of(CellPattern... cells) {
        return new SubrowPattern(null, Quantifier.one(), List.of(cells));
    }

    /** Convenience: subrow with quantifier. */
    public static SubrowPattern of(Quantifier q, CellPattern... cells) {
        return new SubrowPattern(null, q, List.of(cells));
    }
}
