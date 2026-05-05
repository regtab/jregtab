package ru.icc.regtab.itm.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Subrow pattern (Def. 25):
 * P_sr = (ℓ, λ, q, ⟨P_cell¹, …, P_cellᵏ⟩).
 *
 * @param label     optional label for reuse (null if absent)
 * @param condition optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param cellPatterns ordered sequence of cell patterns (≥ 1)
 */
public record SubrowPattern(
        String label,
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
        return new SubrowPattern(null, null, Quantifier.one(), List.of(cells));
    }

    /** Convenience: subrow with quantifier. */
    public static SubrowPattern of(Quantifier q, CellPattern... cells) {
        return new SubrowPattern(null, null, q, List.of(cells));
    }
}
