package ru.icc.regtab.itm.atp.spec;

/**
 * Cell pattern (Def. 24):
 * P_cell = (ℓ, λ, q, CS).
 * <p>
 * If CS is absent (null), the cell is skipped: matched but produces no items.
 *
 * @param label     optional label for reuse (null if absent)
 * @param condition optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param contentSpec optional content specification (null = skip)
 */
public record CellPattern(
        String label,
        CellMatchCondition condition,
        Quantifier quantifier,
        ContentSpec contentSpec
) {
    public CellPattern {
        if (quantifier == null) quantifier = Quantifier.one();
    }

    /** Convenience: skip cell, no condition, quantifier ONE. */
    public static CellPattern skip() {
        return new CellPattern(null, null, Quantifier.one(), null);
    }

    /** Convenience: skip with quantifier. */
    public static CellPattern skip(Quantifier q) {
        return new CellPattern(null, null, q, null);
    }

    /** Convenience: cell with atomic content spec, quantifier ONE. */
    public static CellPattern of(ContentSpec cs) {
        return new CellPattern(null, null, Quantifier.one(), cs);
    }

    /** Convenience: cell with atomic content spec and quantifier. */
    public static CellPattern of(Quantifier q, ContentSpec cs) {
        return new CellPattern(null, null, q, cs);
    }

    /** Convenience: cell with condition, quantifier, and content spec. */
    public static CellPattern of(CellMatchCondition cond, Quantifier q, ContentSpec cs) {
        return new CellPattern(null, cond, q, cs);
    }
}
