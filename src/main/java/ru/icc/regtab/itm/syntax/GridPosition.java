package ru.icc.regtab.itm.syntax;

/**
 * A position in the row-column grid of a table.
 * Corresponds to an element of G = {0,...,m-1} x {0,...,n-1}.
 */
public record GridPosition(int row, int col) implements Comparable<GridPosition> {

    public GridPosition {
        if (row < 0) throw new IllegalArgumentException("row must be non-negative: " + row);
        if (col < 0) throw new IllegalArgumentException("col must be non-negative: " + col);
    }

    @Override
    public int compareTo(GridPosition other) {
        int cmp = Integer.compare(this.row, other.row);
        return cmp != 0 ? cmp : Integer.compare(this.col, other.col);
    }
}
