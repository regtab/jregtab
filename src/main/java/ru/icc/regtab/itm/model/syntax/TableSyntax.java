package ru.icc.regtab.itm.model.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The syntactic layer of an ITM instance: a grid of cells with layout,
 * formatting, and content properties, organized into subtables, rows,
 * and subrows.
 * <p>
 * The constructor creates all cells and a default structure:
 * one {@link Subtable} spanning all rows, one {@link Row} per grid row,
 * and one {@link Subrow} per row spanning all columns.
 * <p>
 * The structure can be refined later via {@link #defineSubtables} and
 * {@link #defineSubrow} (e.g. after RTL pattern matching).
 */
public final class TableSyntax {

    private final int numRows;
    private final int numCols;
    private final Cell[][] grid;
    private final Row[] rows;
    private final List<Subtable> subtables = new ArrayList<>();

    /**
     * Creates a syntactic layer with the given dimensions.
     * All cells are pre-created with default properties.
     * A default structure is established: 1 subtable, N rows, 1 subrow per row.
     */
    public TableSyntax(int numRows, int numCols) {
        if (numRows <= 0) throw new IllegalArgumentException("numRows must be positive: " + numRows);
        if (numCols <= 0) throw new IllegalArgumentException("numCols must be positive: " + numCols);
        this.numRows = numRows;
        this.numCols = numCols;
        this.grid = new Cell[numRows][numCols];
        this.rows = new Row[numRows];

        for (int r = 0; r < numRows; r++) {
            rows[r] = new Row(r);
            Subrow subrow = new Subrow(0, numCols - 1);
            for (int c = 0; c < numCols; c++) {
                Cell cell = new Cell(new GridPosition(r, c));
                grid[r][c] = cell;
                cell.setParentRow(rows[r]);
                subrow.addCell(cell);
            }
            rows[r].addSubrow(subrow);
        }

        Subtable defaultSt = new Subtable(0, numRows - 1);
        for (int r = 0; r < numRows; r++) {
            defaultSt.addRow(rows[r]);
        }
        subtables.add(defaultSt);

        updateCellSubtableRefs();
    }

    public int numRows() { return numRows; }
    public int numCols() { return numCols; }
    public List<Subtable> subtables() { return Collections.unmodifiableList(subtables); }

    /**
     * Returns the row at the given index. Always non-null.
     */
    public Row row(int index) {
        if (index < 0 || index >= numRows) throw new IndexOutOfBoundsException("row: " + index);
        return rows[index];
    }

    /**
     * Returns all rows in order.
     */
    public List<Row> rows() { return List.of(rows); }

    /**
     * Returns the cell at (row, col). Always non-null.
     */
    public Cell getCell(int row, int col) {
        checkBounds(row, col);
        return grid[row][col];
    }

    /**
     * Returns all cells in row-major order.
     */
    public List<Cell> allCells() {
        List<Cell> cells = new ArrayList<>(numRows * numCols);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                cells.add(grid[i][j]);
            }
        }
        return cells;
    }

    /**
     * Redefines the subtable partitioning.
     * Each call replaces all subtables.
     *
     * @param boundaries starting row indices of each subtable (ascending, first must be 0).
     *                   E.g. {@code defineSubtables(0, 3)} creates two subtables:
     *                   rows 0-2 and rows 3-(numRows-1).
     */
    public void defineSubtables(int... boundaries) {
        if (boundaries.length == 0) {
            throw new IllegalArgumentException("At least one boundary is required");
        }
        if (boundaries[0] != 0) {
            throw new IllegalArgumentException("First boundary must be 0, got: " + boundaries[0]);
        }
        for (int i = 1; i < boundaries.length; i++) {
            if (boundaries[i] <= boundaries[i - 1]) {
                throw new IllegalArgumentException("Boundaries must be strictly ascending: "
                        + boundaries[i - 1] + " >= " + boundaries[i]);
            }
            if (boundaries[i] >= numRows) {
                throw new IllegalArgumentException("Boundary out of range: " + boundaries[i]);
            }
        }

        subtables.clear();
        for (int i = 0; i < boundaries.length; i++) {
            int rowStart = boundaries[i];
            int rowEnd = (i + 1 < boundaries.length) ? boundaries[i + 1] - 1 : numRows - 1;
            Subtable st = new Subtable(rowStart, rowEnd);
            for (int r = rowStart; r <= rowEnd; r++) {
                st.addRow(rows[r]);
            }
            subtables.add(st);
        }
        updateCellSubtableRefs();
    }

    /**
     * Defines a subrow within the specified row.
     * On the first call for a given row, the default (full-width) subrow is replaced.
     * Subsequent calls add additional subrows.
     * The caller must ensure that all columns are covered without overlap.
     *
     * @param rowIndex the row index
     * @param colStart first column index (inclusive)
     * @param colEnd   last column index (inclusive)
     */
    public void defineSubrow(int rowIndex, int colStart, int colEnd) {
        if (rowIndex < 0 || rowIndex >= numRows) throw new IndexOutOfBoundsException("rowIndex: " + rowIndex);
        if (colStart < 0 || colStart >= numCols) throw new IndexOutOfBoundsException("colStart: " + colStart);
        if (colEnd < colStart || colEnd >= numCols) {
            throw new IllegalArgumentException("colEnd must be in [colStart, numCols-1]: " + colEnd);
        }

        Row row = rows[rowIndex];
        boolean isDefault = row.subrows().size() == 1
                && row.subrows().getFirst().colStart() == 0
                && row.subrows().getFirst().colEnd() == numCols - 1;

        if (isDefault) {
            row.clearSubrows();
        }

        Subrow subrow = new Subrow(colStart, colEnd);
        for (int c = colStart; c <= colEnd; c++) {
            subrow.addCell(grid[rowIndex][c]);
        }
        row.addSubrow(subrow);
    }

    private void updateCellSubtableRefs() {
        for (Subtable st : subtables) {
            for (Row row : st.rows()) {
                for (Subrow sr : row.subrows()) {
                    for (Cell cell : sr.cells()) {
                        cell.setSubtable(st);
                    }
                }
            }
        }
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= numRows) throw new IndexOutOfBoundsException("row: " + row);
        if (col < 0 || col >= numCols) throw new IndexOutOfBoundsException("col: " + col);
    }
}
