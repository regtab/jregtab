package ru.icc.regtab.atp.match;

import ru.icc.regtab.atp.spec.SubtablePattern;

import java.util.Objects;

/**
 * Matched subtable pattern and the row interval it consumes.
 * <p>
 * The interval {@code [rowStart, rowEnd]} is inclusive. A subtable pattern whose row
 * patterns all matched zero rows yields an <em>empty</em> match:
 * {@code rowEnd == rowStart - 1}, where {@code rowStart} is the position at which the
 * empty match occurred. Empty matches are recorded for completeness but never
 * introduce a subtable boundary in the ITM.
 */
public record MatchedSubtable(SubtablePattern pattern, int rowStart, int rowEnd) {

    public MatchedSubtable {
        Objects.requireNonNull(pattern, "pattern");
        if (rowStart < 0) {
            throw new IllegalArgumentException("rowStart must be non-negative: " + rowStart);
        }
        if (rowEnd < rowStart - 1) {
            throw new IllegalArgumentException("rowEnd must be >= rowStart - 1: " + rowEnd + " < " + (rowStart - 1));
        }
    }

    /** An empty (zero-height) match of {@code pattern} at row {@code row}. */
    public static MatchedSubtable empty(SubtablePattern pattern, int row) {
        return new MatchedSubtable(pattern, row, row - 1);
    }

    /** {@code true} if this match consumed no rows. */
    public boolean isEmpty() {
        return rowEnd < rowStart;
    }

    /** Number of rows consumed (0 for an empty match). */
    public int height() {
        return rowEnd - rowStart + 1;
    }
}
