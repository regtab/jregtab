package ru.icc.regtab.itm.atp.spec;

/**
 * Cell pattern (def:cp):
 * P_cell = (λ, q, S_cont).
 * <p>
 * If S_cont is absent (null), the cell is skipped: matched but produces no items.
 *
 * @param condition  optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param contentSpec content specification S_cont (null = skip)
 */
public record CellPattern(
        CellMatchCondition condition,
        Quantifier quantifier,
        ContentSpec contentSpec
) {
    public CellPattern {
        if (quantifier == null) quantifier = Quantifier.one();
    }

    /** Convenience: skip cell, no condition, quantifier ONE. */
    public static CellPattern skip() {
        return new CellPattern(null, Quantifier.one(), null);
    }

    /** Convenience: skip with quantifier. */
    public static CellPattern skip(Quantifier q) {
        return new CellPattern(null, q, null);
    }

    /** Convenience: cell with atomic content spec, quantifier ONE. */
    public static CellPattern of(ContentSpec cs) {
        return new CellPattern(null, Quantifier.one(), cs);
    }

    /** Convenience: cell with atomic content spec and quantifier. */
    public static CellPattern of(Quantifier q, ContentSpec cs) {
        return new CellPattern(null, q, cs);
    }

    /** Convenience: cell with condition, quantifier, and content spec. */
    public static CellPattern of(CellMatchCondition cond, Quantifier q, ContentSpec cs) {
        return new CellPattern(cond, q, cs);
    }
}
