package ru.icc.regtab.atp.match;

import ru.icc.regtab.atp.spec.SubrowPattern;

import java.util.Objects;

/**
 * Matched subrow pattern and the cell interval it consumes within a row.
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
        if (colEnd < colStart) {
            throw new IllegalArgumentException("colEnd must be >= colStart: " + colEnd + " < " + colStart);
        }
    }
}
