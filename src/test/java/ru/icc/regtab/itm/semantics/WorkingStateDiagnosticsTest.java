package ru.icc.regtab.itm.semantics;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.operation.RecordKey;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The diagnostic channel of the working state: {@link WorkingState#report} records a
 * {@link Diagnostic} (or raises under strict preconditions), and the concatenated-away
 * anchors {@code C} are tracked so that a later action on such an anchor can be told apart
 * from an anchor that never had a record.
 */
class WorkingStateDiagnosticsTest {

    private final TableSyntax syntax = new TableSyntax(4, 4);

    private CellDerivedItem val(int r, int c, String text) {
        syntax.getCell(r, c).setText(text);
        return new CellDerivedItem(text, 0, syntax.getCell(r, c), ItemType.VALUE);
    }

    @Test
    void report_addsDiagnostic() {
        CellDerivedItem a = val(0, 0, "a");
        WorkingState ws = new WorkingState();

        ws.report(a, "JOIN", "anchor has no record — REC missing?");

        assertEquals(1, ws.diagnostics().size());
        Diagnostic d = ws.diagnostics().getFirst();
        assertSame(a, d.anchor());
        assertEquals("JOIN", d.operation());
        assertTrue(d.toString().contains("REC missing"), d.toString());
    }

    @Test
    void report_strictPreconditions_throwsWithSameText() {
        CellDerivedItem a = val(0, 0, "a");
        WorkingState ws = new WorkingState(true);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> ws.report(a, "CONCAT", "anchor has no record — REC missing?"));
        assertEquals(new Diagnostic(a, "CONCAT", "anchor has no record — REC missing?").toString(), e.getMessage());
        assertEquals(1, ws.diagnostics().size(), "the diagnostic is recorded before raising");
    }

    @Test
    void applyConcat_tracksConcatenatedAwayAnchors() {
        CellDerivedItem a1 = val(1, 0, "A"), x = val(1, 1, "x");
        CellDerivedItem a2 = val(2, 0, "A"), y = val(2, 1, "y");
        WorkingState ws = new WorkingState();
        for (CellDerivedItem i : List.of(a1, x, a2, y)) ws.initVal(i, i.str());
        ws.applyRec(a1, List.of(x));
        ws.applyRec(a2, List.of(y));
        assertFalse(ws.isConcatenated(a2));

        ws.applyConcat(a1, List.of(a2), RecordKey.positions(0));

        assertFalse(ws.hasRec(a2));
        assertTrue(ws.isConcatenated(a2), "a2 ∈ C");
        assertFalse(ws.isConcatenated(a1));
        assertEquals(Set.of(a2), ws.allConcatenated());
        assertTrue(ws.diagnostics().isEmpty());
    }
}
