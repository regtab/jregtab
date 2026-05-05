package ru.icc.regtab.itm.model.semantics.predicate;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;

/**
 * Entry point for fluent predicate checks on a candidate item.
 */
public final class Is {

    private final CellDerivedItem candidate;
    /** Fluent checks for candidate position: row, col, subtable, subrow, cell, pos. */
    public final InCheck in;

    public Is(CellDerivedItem candidate) {
        this.candidate = candidate;
        this.in = new InCheck(candidate);
    }

    /**
     * Candidate row &lt; anchor row (candidate is above anchor).
     */
    public DirectionalModifier above(CellDerivedItem anchor) {
        return new DirectionalModifier(candidate, anchor,
                candidate.cell().row() < anchor.cell().row());
    }

    /**
     * Candidate row &gt; anchor row (candidate is below anchor).
     */
    public DirectionalModifier below(CellDerivedItem anchor) {
        return new DirectionalModifier(candidate, anchor,
                candidate.cell().row() > anchor.cell().row());
    }

    /**
     * Candidate col &lt; anchor col (candidate is left of anchor).
     */
    public DirectionalModifier leftOf(CellDerivedItem anchor) {
        return new DirectionalModifier(candidate, anchor,
                candidate.cell().col() < anchor.cell().col());
    }

    /**
     * Candidate col &gt; anchor col (candidate is right of anchor).
     */
    public DirectionalModifier rightOf(CellDerivedItem anchor) {
        return new DirectionalModifier(candidate, anchor,
                candidate.cell().col() > anchor.cell().col());
    }
}
