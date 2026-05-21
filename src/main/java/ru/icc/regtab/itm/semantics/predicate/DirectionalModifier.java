package ru.icc.regtab.itm.semantics.predicate;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.syntax.Cell;

/**
 * Fluent modifier for directional checks: above, below, leftOf, rightOf.
 * Base check is combined with optional constraints (sameCol, sameRow, etc.).
 */
public final class DirectionalModifier {

    private final CellDerivedItem candidate;
    private final CellDerivedItem anchor;
    private final boolean baseCheck;
    /** Range check: rows.from(lo).to(hi) yields baseCheck && lo <= row <= hi. */
    public final DirectionalRange rows;
    /** Range check: cols.from(lo).to(hi) yields baseCheck && lo <= col <= hi. */
    public final DirectionalRange cols;

    public DirectionalModifier(CellDerivedItem candidate, CellDerivedItem anchor, boolean baseCheck) {
        this.candidate = candidate;
        this.anchor = anchor;
        this.baseCheck = baseCheck;
        this.rows = new DirectionalRange(baseCheck, () -> candidate.cell().row());
        this.cols = new DirectionalRange(baseCheck, () -> candidate.cell().col());
    }

    /** Returns the base directional check result (without additional constraints). */
    public boolean check() {
        return baseCheck;
    }

    public boolean sameCol() {
        return baseCheck && candidate.cell().col() == anchor.cell().col();
    }

    public boolean sameRow() {
        return baseCheck && candidate.cell().row() == anchor.cell().row();
    }

    public boolean col(int j) {
        return baseCheck && candidate.cell().col() == j;
    }

    public boolean row(int i) {
        return baseCheck && candidate.cell().row() == i;
    }

    public boolean sameSubtable() {
        Cell c = candidate.cell();
        Cell a = anchor.cell();
        return baseCheck && c.subtable() != null && c.subtable() == a.subtable();
    }

    public boolean sameSubrow() {
        Cell c = candidate.cell();
        Cell a = anchor.cell();
        return baseCheck && c.subrow() != null && c.subrow() == a.subrow();
    }
}
