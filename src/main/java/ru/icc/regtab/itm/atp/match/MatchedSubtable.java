package ru.icc.regtab.itm.atp.match;

import ru.icc.regtab.itm.atp.spec.SubtablePattern;

import java.util.Objects;

/**
 * Matched subtable pattern and the row interval it consumes.
 */
public record MatchedSubtable(SubtablePattern pattern, int rowStart, int rowEnd) {

    public MatchedSubtable {
        Objects.requireNonNull(pattern, "pattern");
        if (rowStart < 0) {
            throw new IllegalArgumentException("rowStart must be non-negative: " + rowStart);
        }
        if (rowEnd < rowStart) {
            throw new IllegalArgumentException("rowEnd must be >= rowStart: " + rowEnd + " < " + rowStart);
        }
    }
}
