package ru.icc.regtab.itm.model.semantics.predicate;

import java.util.function.IntSupplier;

/**
 * Start of range chain: from(lo).to(hi) yields lo <= value <= hi.
 */
public final class IntRangeStart {

    private final IntSupplier valueSupplier;

    public IntRangeStart(IntSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public IntRangeBuilder from(int from) {
        return new IntRangeBuilder(valueSupplier, from);
    }
}
