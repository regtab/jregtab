package ru.icc.regtab.atp.match;

import ru.icc.regtab.atp.spec.SubrowPattern;

import java.util.Objects;

/**
 * Matched subrow pattern and the cell interval it consumes within a row.
 * <p>
 * The interval {@code [colStart, colEnd]} is inclusive. A subrow pattern whose cell
 * patterns all matched zero cells (e.g. {@code { [BLANK]* }} in a row without blank
 * cells) yields an <em>empty</em> match: {@code colEnd == colStart - 1}, where
 * {@code colStart} is the position at which the empty match occurred (equal to the
 * number of columns when it occurred at the end of the row). Empty matches are
 * recorded here for completeness but are never materialized as ITM subrows.
 */
public record MatchedSubrow(SubrowPattern pattern, int rowIndex, int colStart, int colEnd) {

    public MatchedSubrow {
        Objects.requireNonNull(pattern, "pattern");
        if (rowIndex < 0) {
            throw new IllegalArgumentException("rowIndex must be non-negative: " + rowIndex);
        }
        if (colStart < 0) {
            throw new IllegalArgumentException("colStart must be non-negative: " + colStart);
        }
        if (colEnd < colStart - 1) {
            throw new IllegalArgumentException("colEnd must be >= colStart - 1: " + colEnd + " < " + (colStart - 1));
        }
    }

    /** An empty (zero-width) match of {@code pattern} at column {@code col} of row {@code rowIndex}. */
    public static MatchedSubrow empty(SubrowPattern pattern, int rowIndex, int col) {
        return new MatchedSubrow(pattern, rowIndex, col, col - 1);
    }

    /** {@code true} if this match consumed no cells. */
    public boolean isEmpty() {
        return colEnd < colStart;
    }

    /** Number of cells consumed (0 for an empty match). */
    public int width() {
        return colEnd - colStart + 1;
    }
}
