package ru.icc.regtab.itm.semantics.predicate;

import ru.icc.regtab.itm.syntax.Cell;

import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Range chain: from(lo).to(hi) yields lo <= value <= hi.
 * <p>
 * The value is obtained from an {@link IntSupplier}, or — through the factories
 * {@link #ofRow(Cell)}, {@link #ofCol(Cell)}, {@link #ofValue(int)} — directly from a cell or a
 * constant, without allocating a capturing lambda per instance (a {@code CellDerivedItem} carries
 * three ranges, so this matters on tables with millions of cells).
 */
public final class IntRange implements IntSupplier {

    private static final int KIND_SUPPLIER = 0;
    private static final int KIND_ROW = 1;
    private static final int KIND_COL = 2;
    private static final int KIND_VALUE = 3;

    private final int kind;
    private final Object target;
    private final int value;

    public IntRange(IntSupplier valueSupplier) {
        this(KIND_SUPPLIER, Objects.requireNonNull(valueSupplier, "valueSupplier"), 0);
    }

    private IntRange(int kind, Object target, int value) {
        this.kind = kind;
        this.target = target;
        this.value = value;
    }

    /** Range over the row index of {@code cell}. */
    public static IntRange ofRow(Cell cell) { return new IntRange(KIND_ROW, Objects.requireNonNull(cell, "cell"), 0); }

    /** Range over the column index of {@code cell}. */
    public static IntRange ofCol(Cell cell) { return new IntRange(KIND_COL, Objects.requireNonNull(cell, "cell"), 0); }

    /** Range over a constant value. */
    public static IntRange ofValue(int value) { return new IntRange(KIND_VALUE, null, value); }

    @Override
    public int getAsInt() {
        return switch (kind) {
            case KIND_ROW -> ((Cell) target).row();
            case KIND_COL -> ((Cell) target).col();
            case KIND_VALUE -> value;
            default -> ((IntSupplier) target).getAsInt();
        };
    }

    public IntRangeBuilder from(int from) {
        return new IntRangeBuilder(this, from);
    }
}
