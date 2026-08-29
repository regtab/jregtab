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
 * O_concat^K (RTL {@code CONCAT(K)}): folds the records of items sharing a key into one wide
 * record. Pins the preconditions: the key positions must agree, and — apart from the key —
 * no named attribute may occur in more than one of the concatenated records; a violation
 * leaves the working state unchanged and is reported as a {@link Diagnostic}
 * (or raises under strict preconditions). Up to 0.5.x a shared attribute was silently
 * deduplicated, which hid the conflict.
 */
class WorkingStateConcatTest {

    private final TableSyntax syntax = new TableSyntax(4, 5);

    private CellDerivedItem val(int r, int c, String text) {
        syntax.getCell(r, c).setText(text);
        return new CellDerivedItem(text, 0, syntax.getCell(r, c), ItemType.VALUE);
    }

    private static void name(WorkingState ws, CellDerivedItem item, String attribute) {
        ws.setAvp(item, attribute, item.str());
    }

    private static WorkingState init(CellDerivedItem... items) {
        WorkingState ws = new WorkingState();
        for (CellDerivedItem i : items) ws.initVal(i, i.str());
        return ws;
    }

    @Test
    void foldsRecordsWithDistinctAttributes_keyNotRepeated() {
        // T-1 | D16       rec(t1a) = <ID:T-1, REF_TP:D16>
        // T-1 | 001       rec(t1b) = <ID:T-1, REF_SN:001>
        CellDerivedItem t1a = val(1, 0, "T-1"), d16 = val(1, 1, "D16");
        CellDerivedItem t1b = val(2, 0, "T-1"), s001 = val(2, 1, "001");
        WorkingState ws = init(t1a, d16, t1b, s001);
        name(ws, t1a, "ID"); name(ws, d16, "REF_TP");
        name(ws, t1b, "ID"); name(ws, s001, "REF_SN");
        ws.applyRec(t1a, List.of(d16));
        ws.applyRec(t1b, List.of(s001));

        ws.applyConcat(t1a, List.of(t1b), RecordKey.positions(0));

        assertEquals(List.of(List.of(t1a, d16, s001)), ws.rec(t1a));
        assertFalse(ws.hasRec(t1b), "the concatenated anchor is removed from dom(rec)");
        assertEquals(Set.of(t1a), ws.allRec().keySet());
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void foldsUnnamedRecords_task016Shape() {
        // book | 5 ; book | 6 ; book | 7  ->  book,5,6,7
        CellDerivedItem b1 = val(1, 0, "book"), five = val(1, 1, "5");
        CellDerivedItem b2 = val(2, 0, "book"), six = val(2, 1, "6");
        CellDerivedItem b3 = val(3, 0, "book"), seven = val(3, 1, "7");
        WorkingState ws = init(b1, five, b2, six, b3, seven);
        ws.applyRec(b1, List.of(five));
        ws.applyRec(b2, List.of(six));
        ws.applyRec(b3, List.of(seven));

        ws.applyConcat(b1, List.of(b2, b3), RecordKey.positions(0));

        assertEquals(List.of(List.of(b1, five, six, seven)), ws.rec(b1));
        assertFalse(ws.hasRec(b2));
        assertFalse(ws.hasRec(b3));
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void sharedNamedAttribute_noEffectAndDiagnostic() {
        // A | 5   rec = <ID:A, Qty:5>
        // A | 7   rec = <ID:A, Qty:7>      -- Qty occurs in both: not a key, a conflict
        CellDerivedItem a1 = val(1, 0, "A"), five = val(1, 1, "5");
        CellDerivedItem a2 = val(2, 0, "A"), seven = val(2, 1, "7");
        WorkingState ws = init(a1, five, a2, seven);
        name(ws, a1, "ID"); name(ws, five, "Qty");
        name(ws, a2, "ID"); name(ws, seven, "Qty");
        ws.applyRec(a1, List.of(five));
        ws.applyRec(a2, List.of(seven));

        ws.applyConcat(a1, List.of(a2), RecordKey.positions(0));

        assertEquals(List.of(List.of(a1, five)), ws.rec(a1), "anchor record unchanged");
        assertEquals(List.of(List.of(a2, seven)), ws.rec(a2), "the other record is kept: both survive");
        assertEquals(2, ws.allRec().size());
        assertEquals(1, ws.diagnostics().size());
        Diagnostic d = ws.diagnostics().getFirst();
        assertSame(a1, d.anchor());
        assertEquals("CONCAT", d.operation());
        assertTrue(d.message().contains("Qty"), d.message());
    }

    @Test
    void keyPositionMismatch_noEffectAndDiagnostic() {
        CellDerivedItem x = val(1, 0, "X"), five = val(1, 1, "5");
        CellDerivedItem y = val(2, 0, "Y"), seven = val(2, 1, "7");
        WorkingState ws = init(x, five, y, seven);
        ws.applyRec(x, List.of(five));
        ws.applyRec(y, List.of(seven));

        ws.applyConcat(x, List.of(y), RecordKey.positions(0));

        assertEquals(List.of(List.of(x, five)), ws.rec(x));
        assertTrue(ws.hasRec(y));
        assertEquals(1, ws.diagnostics().size());
        assertTrue(ws.diagnostics().getFirst().message().contains("key position 0"));
    }

    @Test
    void strictPreconditions_throwWithTheSameMessage() {
        CellDerivedItem a1 = val(1, 0, "A"), five = val(1, 1, "5");
        CellDerivedItem a2 = val(2, 0, "A"), seven = val(2, 1, "7");
        WorkingState ws = new WorkingState(true);
        for (CellDerivedItem i : List.of(a1, five, a2, seven)) ws.initVal(i, i.str());
        name(ws, a1, "ID"); name(ws, five, "Qty");
        name(ws, a2, "ID"); name(ws, seven, "Qty");
        ws.applyRec(a1, List.of(five));
        ws.applyRec(a2, List.of(seven));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> ws.applyConcat(a1, List.of(a2), RecordKey.positions(0)));
        assertTrue(e.getMessage().contains("Qty"), e.getMessage());
        assertEquals(1, ws.diagnostics().size(), "the diagnostic is recorded before throwing");
    }

    @Test
    void itemsWithoutRecordsAreIgnored() {
        CellDerivedItem b1 = val(1, 0, "book"), five = val(1, 1, "5");
        CellDerivedItem stray = val(2, 0, "book");
        WorkingState ws = init(b1, five, stray);
        ws.applyRec(b1, List.of(five));
        List<List<Item>> before = ws.rec(b1);

        ws.applyConcat(b1, List.of(stray), RecordKey.positions(0));

        assertEquals(before, ws.rec(b1));
        assertTrue(ws.diagnostics().isEmpty(), "no record to concatenate is not a violation");
    }

    // --- Named key: K may name attributes, resolved to a position per record ---

    @Test
    void namedKey_resolvedPerRecord_evenWhenTheAttributeSitsAtDifferentPositions() {
        // rec(r1) = <k, c1, a1:A>      A at position 2
        // rec(r2) = <k, a1:A, c2>      A at position 1
        CellDerivedItem r1 = val(1, 0, "k"), c1 = val(1, 1, "c1"), a1 = val(1, 2, "a1");
        CellDerivedItem r2 = val(2, 0, "k"), a1b = val(2, 1, "a1"), c2 = val(2, 2, "c2");
        WorkingState ws = init(r1, c1, a1, r2, a1b, c2);
        name(ws, a1, "A"); name(ws, a1b, "A");
        ws.applyRec(r1, List.of(c1, a1));
        ws.applyRec(r2, List.of(a1b, c2));

        ws.applyConcat(r1, List.of(r2), RecordKey.of(Set.of(0), Set.of("A")));

        assertEquals(List.of(List.of(r1, c1, a1, c2)), ws.rec(r1),
                "the anchor keeps its key; A is dropped from the concatenated record at its own position (1, not 2)");
        assertFalse(ws.hasRec(r2));
        assertTrue(ws.diagnostics().isEmpty());
    }

    @Test
    void namedKey_missingInARecord_noEffectAndDiagnostic() {
        CellDerivedItem r1 = val(1, 0, "k"), a1 = val(1, 1, "a1");
        CellDerivedItem r2 = val(2, 0, "k"), c2 = val(2, 1, "c2");
        WorkingState ws = init(r1, a1, r2, c2);
        name(ws, a1, "A");
        ws.applyRec(r1, List.of(a1));
        ws.applyRec(r2, List.of(c2));

        ws.applyConcat(r1, List.of(r2), RecordKey.names("A"));

        assertEquals(List.of(List.of(r1, a1)), ws.rec(r1));
        assertTrue(ws.hasRec(r2), "no effect: both records remain");
        assertEquals(1, ws.diagnostics().size());
        Diagnostic d = ws.diagnostics().getFirst();
        assertEquals("CONCAT", d.operation());
        assertTrue(d.message().contains("key attribute 'A' is missing"), d.message());
    }

    @Test
    void namedKey_valuesDiffer_noEffectAndDiagnostic() {
        CellDerivedItem r1 = val(1, 0, "k"), a1 = val(1, 1, "a1");
        CellDerivedItem r2 = val(2, 0, "k"), a2 = val(2, 1, "a2");
        WorkingState ws = init(r1, a1, r2, a2);
        name(ws, a1, "A"); name(ws, a2, "A");
        ws.applyRec(r1, List.of(a1));
        ws.applyRec(r2, List.of(a2));

        ws.applyConcat(r1, List.of(r2), RecordKey.of(Set.of(0), Set.of("A")));

        assertTrue(ws.hasRec(r2));
        assertEquals(1, ws.diagnostics().size());
        assertTrue(ws.diagnostics().getFirst().message().contains("key attribute 'A' differs"),
                ws.diagnostics().getFirst().message());
    }

    @Test
    void mixedKey_isEquivalentToThePositionalKey_task098Shape() {
        // k1 | k11 | a1:A | b1:B | c1        CONCAT(0,1,2,3) == CONCAT(0,1,'A','B')
        // k1 | k11 | a1:A | b1:B | c2
        WorkingState[] states = new WorkingState[2];
        CellDerivedItem[] anchors = new CellDerivedItem[2];
        CellDerivedItem[][] tails = new CellDerivedItem[2][];
        for (int v = 0; v < 2; v++) {
            CellDerivedItem k1 = val(1, 0, "k1"), k11 = val(1, 1, "k11"), a1 = val(1, 2, "a1"), b1 = val(1, 3, "b1"), c1 = val(1, 4, "c1");
            CellDerivedItem k1b = val(2, 0, "k1"), k11b = val(2, 1, "k11"), a1b = val(2, 2, "a1"), b1b = val(2, 3, "b1"), c2 = val(2, 4, "c2");
            WorkingState ws = init(k1, k11, a1, b1, c1, k1b, k11b, a1b, b1b, c2);
            name(ws, a1, "A"); name(ws, b1, "B"); name(ws, a1b, "A"); name(ws, b1b, "B");
            ws.applyRec(k1, List.of(k11, a1, b1, c1));
            ws.applyRec(k1b, List.of(k11b, a1b, b1b, c2));
            RecordKey key = v == 0 ? RecordKey.positions(0, 1, 2, 3) : RecordKey.of(Set.of(0, 1), Set.of("A", "B"));
            ws.applyConcat(k1, List.of(k1b), key);
            states[v] = ws; anchors[v] = k1; tails[v] = new CellDerivedItem[]{k11, a1, b1, c1, c2};
        }
        for (int v = 0; v < 2; v++) {
            assertEquals(List.of(List.of(anchors[v], tails[v][0], tails[v][1], tails[v][2], tails[v][3], tails[v][4])),
                    states[v].rec(anchors[v]));
            assertTrue(states[v].diagnostics().isEmpty());
        }
    }
}
