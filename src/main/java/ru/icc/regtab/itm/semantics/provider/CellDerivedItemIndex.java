package ru.icc.regtab.itm.semantics.provider;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.syntax.Subtable;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Spatial index over a target set J of cell-derived items, shared by all
 * {@link CellDerivedItemProvider}s of one semantic layer.
 * <p>
 * The index keeps two orderings of J — row-major and column-major, exactly as produced by
 * {@link ItemLinearization} — together with row / column offsets, so that a
 * {@link CandidateScope} resolves to a contiguous slice of one of them:
 * a row, a column, a cell (binary search inside a row), a row range (a subtable is a row range),
 * or the whole set. Every slice is already in traversal order, so the provider needs neither
 * a copy of J nor a sort.
 * <p>
 * The index is built lazily on first {@link #lookup} and rebuilt whenever the size of the
 * underlying set has changed since the last build — the set may grow after providers are created.
 */
public final class CellDerivedItemIndex {

    private final Set<CellDerivedItem> items;

    private int indexedSize = -1;
    private int numRows;
    private int numCols;
    private CellDerivedItem[] rowMajor;
    private int[] rowStart;
    private CellDerivedItem[] colMajor;
    private int[] colStart;
    /** Column-major ordering of one subtable's items, built on demand (see {@link #subtableColMajor}). */
    private Map<Subtable, CellDerivedItem[]> subtableColMajor;

    private static final CellDerivedItem[] NONE = new CellDerivedItem[0];

    public CellDerivedItemIndex(Set<CellDerivedItem> items) {
        this.items = Objects.requireNonNull(items, "items");
    }

    /** The live target set J (not a copy). */
    public Set<CellDerivedItem> items() { return items; }

    /**
     * A contiguous slice {@code array[from, to)} of an ordered array, to be traversed forward or,
     * when {@code backward} is set, from the end — cell by cell, keeping the ascending index order
     * inside each cell (this is exactly the order of {@link ItemLinearization} for the reverse
     * traversal orders).
     */
    public record Range(CellDerivedItem[] array, int from, int to, boolean backward) {
        public static final Range EMPTY = new Range(NONE, 0, 0, false);
        public boolean isEmpty() { return from >= to; }
    }

    /**
     * Resolves the scope against the anchor and returns the candidate slice in traversal order τ.
     * The slice is a superset of {@code {ι ∈ J | ι lies in scope(anchor)}}.
     */
    public Range lookup(CandidateScope scope, CellDerivedItem anchor, TraversalOrder order) {
        ensureBuilt();
        if (rowMajor.length == 0) return Range.EMPTY;

        boolean backward = order == TraversalOrder.REVERSE_ROW_MAJOR || order == TraversalOrder.REVERSE_COLUMN_MAJOR;
        boolean columnMajor = order == TraversalOrder.COLUMN_MAJOR || order == TraversalOrder.REVERSE_COLUMN_MAJOR;

        if (scope.isAll()) {
            CellDerivedItem[] a = columnMajor ? colMajor : rowMajor;
            return new Range(a, 0, a.length, backward);
        }

        int rLo = scope.rowLo(anchor, numRows), rHi = scope.rowHi(anchor, numRows);
        int cLo = scope.colLo(anchor, numCols), cHi = scope.colHi(anchor, numCols);
        Subtable st = anchor.cell().subtable();
        if (scope.sameSubtable()) {
            if (st == null) return Range.EMPTY;
            rLo = Math.max(rLo, st.rowStart());
            rHi = Math.min(rHi, st.rowEnd());
        }
        if (rLo > rHi || cLo > cHi) return Range.EMPTY;

        if (rLo == rHi) {
            // one row: rowMajor slice sorted by (col, index); narrow by columns
            int from = rowStart[rLo], to = rowStart[rLo + 1];
            if (scope.cols() != null) {
                from = lowerBoundCol(rowMajor, from, to, cLo);
                to = upperBoundCol(rowMajor, from, to, cHi);
            }
            return new Range(rowMajor, from, to, backward);
        }
        if (cLo == cHi) {
            // one column: colMajor slice sorted by (row, index); narrow by rows
            int from = colStart[cLo], to = colStart[cLo + 1];
            if (scope.rows() != null || scope.sameSubtable()) {
                from = lowerBoundRow(colMajor, from, to, rLo);
                to = upperBoundRow(colMajor, from, to, rHi);
            }
            return new Range(colMajor, from, to, backward);
        }
        if (!columnMajor) {
            // row range (a subtable is a row range) in row-major order
            return new Range(rowMajor, rowStart[rLo], rowStart[rHi + 1], backward);
        }
        if (scope.sameSubtable()) {
            CellDerivedItem[] a = subtableColMajor(st);
            int from = lowerBoundCol(a, 0, a.length, cLo);
            int to = upperBoundCol(a, from, a.length, cHi);
            return new Range(a, from, to, backward);
        }
        return new Range(colMajor, colStart[cLo], colStart[cHi + 1], backward);
    }

    // --- build ---

    private void ensureBuilt() {
        if (indexedSize == items.size()) return;
        build();
    }

    private void build() {
        CellDerivedItem[] a = items.toArray(NONE);
        int n = a.length;
        int maxRow = -1, maxCol = -1;
        for (CellDerivedItem item : a) {
            maxRow = Math.max(maxRow, item.cell().row());
            maxCol = Math.max(maxCol, item.cell().col());
        }
        numRows = maxRow + 1;
        numCols = maxCol + 1;

        rowMajor = a.clone();
        Arrays.sort(rowMajor, ItemLinearization.comparator(TraversalOrder.ROW_MAJOR));   // stable
        colMajor = a;
        Arrays.sort(colMajor, ItemLinearization.comparator(TraversalOrder.COLUMN_MAJOR));

        rowStart = new int[numRows + 1];
        int idx = 0;
        for (int r = 0; r < numRows; r++) {
            rowStart[r] = idx;
            while (idx < n && rowMajor[idx].cell().row() == r) idx++;
        }
        rowStart[numRows] = n;

        colStart = new int[numCols + 1];
        idx = 0;
        for (int c = 0; c < numCols; c++) {
            colStart[c] = idx;
            while (idx < n && colMajor[idx].cell().col() == c) idx++;
        }
        colStart[numCols] = n;

        subtableColMajor = null;
        indexedSize = n;
    }

    /** Items of the subtable (its row range) in column-major order; built once per subtable. */
    private CellDerivedItem[] subtableColMajor(Subtable st) {
        if (subtableColMajor == null) subtableColMajor = new IdentityHashMap<>();
        CellDerivedItem[] a = subtableColMajor.get(st);
        if (a == null) {
            int rLo = Math.max(0, st.rowStart());
            int rHi = Math.min(numRows - 1, st.rowEnd());
            a = rLo > rHi ? NONE : Arrays.copyOfRange(rowMajor, rowStart[rLo], rowStart[rHi + 1]);
            Arrays.sort(a, ItemLinearization.comparator(TraversalOrder.COLUMN_MAJOR));
            subtableColMajor.put(st, a);
        }
        return a;
    }

    // --- binary search on slices sorted by col (row slices, subtable column-major) or by row (column slices) ---

    private static int lowerBoundCol(CellDerivedItem[] a, int from, int to, int col) {
        int lo = from, hi = to;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].cell().col() < col) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private static int upperBoundCol(CellDerivedItem[] a, int from, int to, int col) {
        int lo = from, hi = to;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].cell().col() <= col) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private static int lowerBoundRow(CellDerivedItem[] a, int from, int to, int row) {
        int lo = from, hi = to;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].cell().row() < row) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private static int upperBoundRow(CellDerivedItem[] a, int from, int to, int row) {
        int lo = from, hi = to;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].cell().row() <= row) lo = mid + 1; else hi = mid;
        }
        return lo;
    }
}
