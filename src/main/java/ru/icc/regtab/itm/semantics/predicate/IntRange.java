package ru.icc.regtab.itm.semantics.predicate;

import java.util.function.IntSupplier;

/**
 * Range chain: from(lo).to(hi) yields lo <= value <= hi.
 */
public final class IntRange {

    private final IntSupplier valueSupplier;

    public IntRange(IntSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public IntRangeBuilder from(int from) {
        return new IntRangeBuilder(valueSupplier, from);
    }
}
