package ru.icc.regtab.itm.model.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A consecutive sequence of cells within a single row.
 * Each cell belongs to exactly one subrow.
 */
public final class Subrow {

    private final int colStart;
    private final int colEnd;
    private final List<Cell> cells = new ArrayList<>();

    /**
     * @param colStart first column index (inclusive)
     * @param colEnd   last column index (inclusive)
     */
    public Subrow(int colStart, int colEnd) {
        if (colStart < 0) throw new IllegalArgumentException("colStart must be non-negative: " + colStart);
        if (colEnd < colStart) throw new IllegalArgumentException("colEnd must be >= colStart: " + colEnd + " < " + colStart);
        this.colStart = colStart;
        this.colEnd = colEnd;
    }

    public int colStart() { return colStart; }
    public int colEnd() { return colEnd; }
    public List<Cell> cells() { return Collections.unmodifiableList(cells); }

    void addCell(Cell cell) {
        cell.setSubrow(this);
        cells.add(cell);
    }
}
