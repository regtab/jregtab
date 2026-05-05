package ru.icc.regtab.itm.model.semantics.predicate;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.syntax.Cell;

/**
 * Fluent checks for candidate item position: row, col, subtable, subrow, cell, pos.
 */
public final class InCheck {

    private final CellDerivedItem candidate;
    /** Range check: rows.from(lo).to(hi) yields lo <= row <= hi. */
    public final IntRangeStart rows;
    /** Range check: cols.from(lo).to(hi) yields lo <= col <= hi. */
    public final IntRangeStart cols;
    /** Range check: pos.from(lo).to(hi) yields lo <= index <= hi. */
    public final IntRangeStart pos;

    public InCheck(CellDerivedItem candidate) {
        this.candidate = candidate;
        this.rows = new IntRangeStart(() -> this.candidate.cell().row());
        this.cols = new IntRangeStart(() -> this.candidate.cell().col());
        this.pos = new IntRangeStart(this.candidate::index);
    }

    public boolean sameRow(CellDerivedItem anchor) {
        return candidate.cell().row() == anchor.cell().row();
    }

    public boolean sameCol(CellDerivedItem anchor) {
        return candidate.cell().col() == anchor.cell().col();
    }

    public boolean row(int i) {
        return candidate.cell().row() == i;
    }

    public boolean col(int j) {
        return candidate.cell().col() == j;
    }

    public boolean sameSubtable(CellDerivedItem anchor) {
        Cell c = candidate.cell();
        Cell a = anchor.cell();
        return c.subtable() != null && c.subtable() == a.subtable();
    }

    public boolean sameSubrow(CellDerivedItem anchor) {
        Cell c = candidate.cell();
        Cell a = anchor.cell();
        return c.subrow() != null && c.subrow() == a.subrow();
    }

    public boolean sameCell(CellDerivedItem anchor) {
        return candidate.cell() == anchor.cell();
    }

    public boolean pos(int k) {
        return candidate.index() == k;
    }
}
