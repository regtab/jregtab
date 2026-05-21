package ru.icc.regtab.itm.semantics.predicate;

import java.util.function.IntSupplier;

/**
 * Range chain for directional modifiers: baseCheck && from(lo).to(hi).
 */
public final class DirectionalRange {

    private final boolean baseCheck;
    private final IntSupplier valueSupplier;

    DirectionalRange(boolean baseCheck, IntSupplier valueSupplier) {
        this.baseCheck = baseCheck;
        this.valueSupplier = valueSupplier;
    }

    public DirectionalRangeBuilder from(int from) {
        return new DirectionalRangeBuilder(baseCheck, valueSupplier, from);
    }

    public static final class DirectionalRangeBuilder {
        private final boolean baseCheck;
        private final IntSupplier valueSupplier;
        private final int from;

        DirectionalRangeBuilder(boolean baseCheck, IntSupplier valueSupplier, int from) {
            this.baseCheck = baseCheck;
            this.valueSupplier = valueSupplier;
            this.from = from;
        }

        public boolean to(int to) {
            int v = valueSupplier.getAsInt();
            return baseCheck && from <= v && v <= to;
        }
    }
}
