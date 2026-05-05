package ru.icc.regtab.itm.model.semantics.predicate;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ItemType;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fluent predicate API on CellDerivedItem.
 */
class ItemPredicateFluentApiTest {

    @Test
    void testInSameRowSameCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem a = new CellDerivedItem("A", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem b = new CellDerivedItem("B", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem c = new CellDerivedItem("C", 0, syntax.getCell(1, 0), ItemType.VALUE);

        assertTrue(a.is.in.sameRow(b));
        assertTrue(a.is.in.sameCol(c));
        assertFalse(a.is.in.sameRow(c));
        assertFalse(a.is.in.sameCol(b));
    }

    @Test
    void testInRowCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem item = new CellDerivedItem("X", 0, syntax.getCell(1, 2), ItemType.VALUE);

        assertTrue(item.is.in.row(1));
        assertTrue(item.is.in.col(2));
        assertFalse(item.is.in.row(0));
        assertFalse(item.is.in.col(0));
    }

    @Test
    void testInRowsColsRange() {
        TableSyntax syntax = new TableSyntax(5, 5);
        CellDerivedItem item = new CellDerivedItem("X", 0, syntax.getCell(2, 3), ItemType.VALUE);

        assertTrue(item.is.in.rows.from(1).to(3));
        assertTrue(item.is.in.cols.from(2).to(4));
        assertFalse(item.is.in.rows.from(0).to(1));
        assertFalse(item.is.in.cols.from(0).to(2));
    }

    @Test
    void testAboveBelowLeftOfRightOf() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem center = new CellDerivedItem("C", 0, syntax.getCell(1, 1), ItemType.VALUE);
        CellDerivedItem above = new CellDerivedItem("A", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem below = new CellDerivedItem("B", 0, syntax.getCell(2, 1), ItemType.VALUE);
        CellDerivedItem left = new CellDerivedItem("L", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem right = new CellDerivedItem("R", 0, syntax.getCell(1, 2), ItemType.VALUE);

        assertTrue(above.is.above(center).check());
        assertTrue(below.is.below(center).check());
        assertTrue(left.is.leftOf(center).check());
        assertTrue(right.is.rightOf(center).check());

        assertFalse(center.is.above(above).check());
        assertFalse(center.is.below(below).check());
    }

    @Test
    void testBelowSameCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem top = new CellDerivedItem("T", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem bottom = new CellDerivedItem("B", 0, syntax.getCell(2, 1), ItemType.VALUE);
        CellDerivedItem otherCol = new CellDerivedItem("O", 0, syntax.getCell(2, 0), ItemType.VALUE);

        assertTrue(bottom.is.below(top).sameCol());
        assertFalse(otherCol.is.below(top).sameCol());
    }

    @Test
    void testInSameCell() {
        TableSyntax syntax = new TableSyntax(2, 2);
        CellDerivedItem a = new CellDerivedItem("A", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem b = new CellDerivedItem("B", 1, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem c = new CellDerivedItem("C", 0, syntax.getCell(1, 1), ItemType.VALUE);

        assertTrue(a.is.in.sameCell(b));
        assertFalse(a.is.in.sameCell(c));
    }

    @Test
    void testInPos() {
        TableSyntax syntax = new TableSyntax(1, 1);
        CellDerivedItem item = new CellDerivedItem("X", 2, syntax.getCell(0, 0), ItemType.VALUE);

        assertTrue(item.is.in.pos(2));
        assertFalse(item.is.in.pos(0));
        assertTrue(item.is.in.pos.from(1).to(3));
    }
}
