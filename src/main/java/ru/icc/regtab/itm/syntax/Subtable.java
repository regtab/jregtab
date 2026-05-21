package ru.icc.regtab.itm.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A consecutive range of rows within a table.
 * Each row belongs to exactly one subtable.
 */
public final class Subtable {

    private final int rowStart;
    private final int rowEnd;
    private final List<Row> rows = new ArrayList<>();

    /**
     * @param rowStart first row index (inclusive)
     * @param rowEnd   last row index (inclusive)
     */
    public Subtable(int rowStart, int rowEnd) {
        if (rowStart < 0) throw new IllegalArgumentException("rowStart must be non-negative: " + rowStart);
        if (rowEnd < rowStart) throw new IllegalArgumentException("rowEnd must be >= rowStart: " + rowEnd + " < " + rowStart);
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
    }

    public int rowStart() { return rowStart; }
    public int rowEnd() { return rowEnd; }
    public List<Row> rows() { return Collections.unmodifiableList(rows); }

    void addRow(Row row) {
        row.setSubtable(this);
        rows.add(row);
    }
}
