package ru.icc.regtab.itm.semantics.provider;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;

/**
 * Candidate scope — a static over-approximation of the support of an item filter κ,
 * used by {@link CellDerivedItemProvider} to pick a bucket of {@link CellDerivedItemIndex}
 * instead of scanning the whole target set J.
 * <p>
 * The scope is a row interval × column interval (each optional, absolute or relative to the
 * anchor) plus a {@code sameSubtable} flag. It never changes the result: κ is still applied to
 * every candidate of the scope, so any scope that contains the support of κ is correct, and
 * {@link #ALL} is always correct.
 *
 * @param rows         row interval or {@code null} (no restriction)
 * @param cols         column interval or {@code null} (no restriction)
 * @param sameSubtable candidates lie in the anchor's subtable
 */
public record CandidateScope(Interval rows, Interval cols, boolean sameSubtable) {

    /** No restriction: the whole target set. */
    public static final CandidateScope ALL = new CandidateScope(null, null, false);

    /**
     * Inclusive interval of row or column indices. Open ends use {@link #OPEN_LO} / {@link #OPEN_HI}.
     * When {@code relative} is {@code true}, both bounds are offsets from the anchor's index.
     */
    public record Interval(int lo, int hi, boolean relative) {

        public static final int OPEN_LO = Integer.MIN_VALUE;
        public static final int OPEN_HI = Integer.MAX_VALUE;

        public static Interval absolute(int lo, int hi) { return new Interval(lo, hi, false); }
        public static Interval absolute(int index)      { return new Interval(index, index, false); }
        public static Interval relative(int lo, int hi) { return new Interval(lo, hi, true); }
        public static Interval relative(int delta)      { return new Interval(delta, delta, true); }

        public boolean isPoint() { return lo == hi; }

        /** Resolved lower bound for the given anchor index (unclamped). */
        public long resolveLo(int anchorIndex) {
            return lo == OPEN_LO ? Long.MIN_VALUE : (relative ? (long) anchorIndex + lo : lo);
        }

        /** Resolved upper bound for the given anchor index (unclamped). */
        public long resolveHi(int anchorIndex) {
            return hi == OPEN_HI ? Long.MAX_VALUE : (relative ? (long) anchorIndex + hi : hi);
        }

        /**
         * Conjunction with another interval: exact intersection when both are of the same kind,
         * otherwise the more specific one (a point wins; ties keep {@code this}).
         * The result always contains the intersection of the two.
         */
        public Interval and(Interval other) {
            if (other == null) return this;
            if (relative == other.relative) {
                return new Interval(Math.max(lo, other.lo), Math.min(hi, other.hi), relative);
            }
            if (isPoint()) return this;
            if (other.isPoint()) return other;
            return this;
        }
    }

    public static CandidateScope rows(Interval rows) { return new CandidateScope(rows, null, false); }
    public static CandidateScope cols(Interval cols) { return new CandidateScope(null, cols, false); }
    public static CandidateScope subtable()          { return new CandidateScope(null, null, true); }

    /** Conjunction of two scopes: each component only narrows. */
    public CandidateScope and(CandidateScope other) {
        Interval r = rows == null ? other.rows : rows.and(other.rows);
        Interval c = cols == null ? other.cols : cols.and(other.cols);
        return new CandidateScope(r, c, sameSubtable || other.sameSubtable);
    }

    public boolean isAll() { return rows == null && cols == null && !sameSubtable; }

    /** Resolved row lower bound for the anchor, clamped to {@code [0, numRows]}. */
    public int rowLo(CellDerivedItem anchor, int numRows) {
        return rows == null ? 0 : clampLo(rows.resolveLo(anchor.cell().row()), numRows);
    }

    /** Resolved row upper bound (inclusive) for the anchor, clamped to {@code [-1, numRows - 1]}. */
    public int rowHi(CellDerivedItem anchor, int numRows) {
        return rows == null ? numRows - 1 : clampHi(rows.resolveHi(anchor.cell().row()), numRows);
    }

    /** Resolved column lower bound for the anchor, clamped to {@code [0, numCols]}. */
    public int colLo(CellDerivedItem anchor, int numCols) {
        return cols == null ? 0 : clampLo(cols.resolveLo(anchor.cell().col()), numCols);
    }

    /** Resolved column upper bound (inclusive) for the anchor, clamped to {@code [-1, numCols - 1]}. */
    public int colHi(CellDerivedItem anchor, int numCols) {
        return cols == null ? numCols - 1 : clampHi(cols.resolveHi(anchor.cell().col()), numCols);
    }

    private static int clampLo(long v, int n) {
        return v < 0 ? 0 : (v > n ? n : (int) v);
    }

    private static int clampHi(long v, int n) {
        return v < -1 ? -1 : (v > n - 1 ? n - 1 : (int) v);
    }
}
