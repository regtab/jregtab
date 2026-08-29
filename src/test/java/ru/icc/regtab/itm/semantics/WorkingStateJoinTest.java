package ru.icc.regtab.itm.semantics;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.operation.RecordKey;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O_join^K (RTL {@code JOIN(K)}): the record product. Pins the cross product (K = ∅), the
 * nested-loop order of the resulting records, the equi-join on key positions, the natural-join
 * treatment of a shared named attribute (agree → kept once, disagree → pair dropped), the
 * left-outer behaviour when no pair survives, lazy consumption through J (several anchors may
 * join the same records), and composition (a second join multiplies further).
 */
class WorkingStateJoinTest {

    private final TableSyntax syntax = new TableSyntax(5, 5);

    private CellDerivedItem val(int r, int c, String text) {
        return val(r, c, text, 0);
    }

    private CellDerivedItem val(int r, int c, String text, int index) {
        syntax.getCell(r, c).setText(syntax.getCell(r, c).text() == null ? text : syntax.getCell(r, c).text());
        return new CellDerivedItem(text, index, syntax.getCell(r, c), ItemType.VALUE);
    }

    private static void name(WorkingState ws, CellDerivedItem item, String attribute) {
        ws.setAvp(item, attribute, item.str());
    }

    private static WorkingState init(CellDerivedItem... items) {
        WorkingState ws = new WorkingState();
        for (CellDerivedItem i : items) ws.initVal(i, i.str());
        return ws;
    }

    // id  | x | y          tokens a, b of one cell; rec(1) = <value:1, var:x>, rec(2) = <value:2, var:y>
    // a;b | 1 | 2
    private CellDerivedItem a, b, c1, c2, hx, hy;

    private WorkingState explodeStack() {
        a = val(1, 0, "a", 0); b = val(1, 0, "b", 1);
        c1 = val(1, 1, "1"); c2 = val(1, 2, "2");
        hx = val(0, 1, "x"); hy = val(0, 2, "y");
        WorkingState ws = init(a, b, c1, c2, hx, hy);
        name(ws, a, "id"); name(ws, b, "id");
        name(ws, c1, "value"); name(ws, c2, "value");
        name(ws, hx, "var"); name(ws, hy, "var");
        ws.applyRec(a, List.of());
        ws.applyRec(b, List.of());
        ws.applyRec(c1, List.of(hx));
        ws.applyRec(c2, List.of(hy));
        return ws;
    }

    @Test
    void crossProduct_oneRecordPerJoinedRecord_inNestedLoopOrder() {
        WorkingState ws = explodeStack();

        ws.applyJoin(a, List.of(c1, c2), RecordKey.EMPTY);

        assertEquals(List.of(List.of(a, c1, hx), List.of(a, c2, hy)), ws.rec(a));
        assertTrue(ws.isJoined(c1));
        assertTrue(ws.isJoined(c2));
        assertEquals(Set.of(c1, c2), ws.allJoined());
        assertNotNull(ws.rec(c1), "a joined-away anchor keeps its records");
        assertEquals(List.of(a, b), List.copyOf(ws.allRec().keySet()), "allRec() = live anchors only");
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void lazyConsumption_secondAnchorJoinsTheSameRecords() {
        WorkingState ws = explodeStack();

        ws.applyJoin(a, List.of(c1, c2), RecordKey.EMPTY);
        ws.applyJoin(b, List.of(c1, c2), RecordKey.EMPTY);

        assertEquals(2, ws.rec(a).size());
        assertEquals(List.of(List.of(b, c1, hx), List.of(b, c2, hy)), ws.rec(b));
        assertTrue(ws.isRecordsetConsistent(), "the 'value' attribute of the joined-away anchors does not break uniformity");
    }

    @Test
    void composition_secondJoinMultipliesFurther() {
        WorkingState ws = explodeStack();
        CellDerivedItem d1 = val(2, 3, "p"), d2 = val(2, 4, "q");
        ws.initVal(d1, "p"); ws.initVal(d2, "q");
        name(ws, d1, "w"); name(ws, d2, "w");
        ws.applyRec(d1, List.of());
        ws.applyRec(d2, List.of());

        ws.applyJoin(a, List.of(c1, c2), RecordKey.EMPTY);
        ws.applyJoin(a, List.of(d1, d2), RecordKey.EMPTY);

        assertEquals(List.of(
                List.of(a, c1, hx, d1), List.of(a, c1, hx, d2),
                List.of(a, c2, hy, d1), List.of(a, c2, hy, d2)), ws.rec(a));
    }

    @Test
    void equiJoinOnKeyPosition_dropsMismatchedPairsAndTheJoinedKey() {
        // rec(p1) = <X, Qty:5>   rec(q1) = <X, Unit:kg>   rec(q2) = <Z, Unit:pc>
        CellDerivedItem p1 = val(1, 0, "X"), qty = val(1, 1, "5");
        CellDerivedItem q1 = val(1, 2, "X"), kg = val(1, 3, "kg");
        CellDerivedItem q2 = val(2, 2, "Z"), pc = val(2, 3, "pc");
        WorkingState ws = init(p1, qty, q1, kg, q2, pc);
        name(ws, qty, "Qty"); name(ws, kg, "Unit"); name(ws, pc, "Unit");
        ws.applyRec(p1, List.of(qty));
        ws.applyRec(q1, List.of(kg));
        ws.applyRec(q2, List.of(pc));

        ws.applyJoin(p1, List.of(q1, q2), RecordKey.positions(0));

        assertEquals(List.of(List.of(p1, qty, kg)), ws.rec(p1));
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void noSurvivingPair_anchorKeepsItsRecords_leftOuter() {
        CellDerivedItem p2 = val(1, 0, "Y"), qty = val(1, 1, "8");
        CellDerivedItem q1 = val(1, 2, "X"), kg = val(1, 3, "kg");
        WorkingState ws = init(p2, qty, q1, kg);
        name(ws, qty, "Qty"); name(ws, kg, "Unit");
        ws.applyRec(p2, List.of(qty));
        ws.applyRec(q1, List.of(kg));

        ws.applyJoin(p2, List.of(q1), RecordKey.positions(0));

        assertEquals(List.of(List.of(p2, qty)), ws.rec(p2));
        assertTrue(ws.isJoined(q1), "J is still extended");
        assertEquals(1, ws.diagnostics().size());
        assertEquals("JOIN", ws.diagnostics().getFirst().operation());
    }

    @Test
    void sharedNamedAttribute_isANaturalJoinCondition() {
        // rec(r1) = <Store:S1, Year:2024>; rec(s1) = <Year:2024, Sales:10>; rec(s2) = <Year:2025, Sales:12>
        CellDerivedItem r1 = val(1, 0, "S1"), y1 = val(1, 1, "2024");
        CellDerivedItem s1 = val(2, 0, "2024"), sales10 = val(2, 1, "10");
        CellDerivedItem s2 = val(3, 0, "2025"), sales12 = val(3, 1, "12");
        WorkingState ws = init(r1, y1, s1, sales10, s2, sales12);
        name(ws, r1, "Store"); name(ws, y1, "Year");
        name(ws, s1, "Year"); name(ws, sales10, "Sales");
        name(ws, s2, "Year"); name(ws, sales12, "Sales");
        ws.applyRec(r1, List.of(y1));
        ws.applyRec(s1, List.of(sales10));
        ws.applyRec(s2, List.of(sales12));

        ws.applyJoin(r1, List.of(s1, s2), RecordKey.EMPTY);

        List<List<Item>> records = ws.rec(r1);
        assertEquals(1, records.size(), "the 2025 pair disagrees on Year and is dropped");
        assertEquals(List.of(r1, y1, sales10), records.getFirst(), "Year occurs once (dedup)");
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void itemsWithoutRecordsAreIgnored() {
        WorkingState ws = explodeStack();
        CellDerivedItem stray = val(3, 0, "z");
        ws.initVal(stray, "z");

        ws.applyJoin(a, List.of(stray), RecordKey.EMPTY);

        assertEquals(List.of(List.of(a)), ws.rec(a));
        assertTrue(ws.allJoined().isEmpty());
        assertTrue(ws.diagnostics().isEmpty());
    }

    // --- Named key ---

    @Test
    void namedKey_pairWithoutTheAttributeIsDropped_unlikeTheBareJoin() {
        // rec(p) = <x, 2024:Year>; rec(s1) = <y, 2024:Year, 10:Sales>; rec(s2) = <z, 12:Sales> (no Year)
        CellDerivedItem p = val(1, 0, "x"), py = val(1, 1, "2024");
        CellDerivedItem s1 = val(2, 0, "y"), s1y = val(2, 1, "2024"), s1s = val(2, 2, "10");
        CellDerivedItem s2 = val(3, 0, "z"), s2s = val(3, 2, "12");
        WorkingState ws = init(p, py, s1, s1y, s1s, s2, s2s);
        name(ws, py, "Year"); name(ws, s1y, "Year"); name(ws, s1s, "Sales"); name(ws, s2s, "Sales");
        ws.applyRec(p, List.of(py));
        ws.applyRec(s1, List.of(s1y, s1s));
        ws.applyRec(s2, List.of(s2s));

        ws.applyJoin(p, List.of(s1, s2), RecordKey.names("Year"));

        assertEquals(List.of(List.of(p, py, s1, s1s)), ws.rec(p),
                "only the pair carrying Year on both sides survives; the joined key is not repeated");
        assertEquals(Set.of(s1, s2), ws.allJoined());
    }
}
