package ru.icc.regtab.itm.semantics.predicate;

import java.util.function.IntSupplier;

/**
 * Builder for integer range checks: from(lo).to(hi) yields lo <= value <= hi.
 */
public final class IntRangeBuilder {

    private final IntSupplier valueSupplier;
    private final int from;

    IntRangeBuilder(IntSupplier valueSupplier, int from) {
        this.valueSupplier = valueSupplier;
        this.from = from;
    }

    public boolean to(int to) {
        int v = valueSupplier.getAsInt();
        return from <= v && v <= to;
    }
}
