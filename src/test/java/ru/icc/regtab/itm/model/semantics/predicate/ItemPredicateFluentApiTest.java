package ru.icc.regtab.itm.model.semantics.predicate;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ItemType;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for predicate methods on CellDerivedItem: position and directional checks.
 */
class ItemPredicateFluentApiTest {

    @Test
    void testInSameRowSameCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem a = new CellDerivedItem("A", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem b = new CellDerivedItem("B", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem c = new CellDerivedItem("C", 0, syntax.getCell(1, 0), ItemType.VALUE);

        assertTrue(a.sameRow(b));
        assertTrue(a.sameCol(c));
        assertFalse(a.sameRow(c));
        assertFalse(a.sameCol(b));
    }

    @Test
    void testInRowCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem item = new CellDerivedItem("X", 0, syntax.getCell(1, 2), ItemType.VALUE);

        assertTrue(item.row(1));
        assertTrue(item.col(2));
        assertFalse(item.row(0));
        assertFalse(item.col(0));
    }

    @Test
    void testInRowsColsRange() {
        TableSyntax syntax = new TableSyntax(5, 5);
        CellDerivedItem item = new CellDerivedItem("X", 0, syntax.getCell(2, 3), ItemType.VALUE);

        assertTrue(item.rows.from(1).to(3));
        assertTrue(item.cols.from(2).to(4));
        assertFalse(item.rows.from(0).to(1));
        assertFalse(item.cols.from(0).to(2));
    }

    @Test
    void testAboveBelowLeftOfRightOf() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem center = new CellDerivedItem("C", 0, syntax.getCell(1, 1), ItemType.VALUE);
        CellDerivedItem above = new CellDerivedItem("A", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem below = new CellDerivedItem("B", 0, syntax.getCell(2, 1), ItemType.VALUE);
        CellDerivedItem left = new CellDerivedItem("L", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem right = new CellDerivedItem("R", 0, syntax.getCell(1, 2), ItemType.VALUE);

        assertTrue(above.above(center).check());
        assertTrue(below.below(center).check());
        assertTrue(left.leftOf(center).check());
        assertTrue(right.rightOf(center).check());

        assertFalse(center.above(above).check());
        assertFalse(center.below(below).check());
    }

    @Test
    void testBelowSameCol() {
        TableSyntax syntax = new TableSyntax(3, 3);
        CellDerivedItem top = new CellDerivedItem("T", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem bottom = new CellDerivedItem("B", 0, syntax.getCell(2, 1), ItemType.VALUE);
        CellDerivedItem otherCol = new CellDerivedItem("O", 0, syntax.getCell(2, 0), ItemType.VALUE);

        assertTrue(bottom.below(top).sameCol());
        assertFalse(otherCol.below(top).sameCol());
    }

    @Test
    void testInSameCell() {
        TableSyntax syntax = new TableSyntax(2, 2);
        CellDerivedItem a = new CellDerivedItem("A", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem b = new CellDerivedItem("B", 1, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem c = new CellDerivedItem("C", 0, syntax.getCell(1, 1), ItemType.VALUE);

        assertTrue(a.sameCell(b));
        assertFalse(a.sameCell(c));
    }

    @Test
    void testInPos() {
        TableSyntax syntax = new TableSyntax(1, 1);
        CellDerivedItem item = new CellDerivedItem("X", 2, syntax.getCell(0, 0), ItemType.VALUE);

        assertTrue(item.posIndex(2));
        assertFalse(item.posIndex(0));
        assertTrue(item.pos.from(1).to(3));
    }
}
