package ru.icc.regtab.itm.interpret;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.TableSemantics;
import ru.icc.regtab.itm.model.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.model.semantics.item.*;
import ru.icc.regtab.itm.model.semantics.operation.*;
import ru.icc.regtab.itm.model.semantics.provider.*;
import ru.icc.regtab.itm.model.syntax.*;
import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduces Example 2 from the paper: equipment table with O_prefix and O_concat.
 *
 * Table (5 rows x 5 cols):
 *      | REF | REF  | SPECS | SPECS
 * T-1  | TP  | D16  | HV    | 750
 * T-1  | SN  | 001  | LV    | 110
 * T-2  | TP  | D24  | HV    | 110
 * T-2  | SN  | 002  | LV    | 10
 *
 * Expected schema: S = <ID, REF_TP, SPECS_HV, REF_SN, SPECS_LV>
 */
class EquipmentTest {

    @Test
    void testEquipment() {
        TableSyntax syntax = new TableSyntax(5, 5);
        syntax.getCell(0, 0).setText("");
        syntax.getCell(0, 1).setText("REF"); syntax.getCell(0, 2).setText("REF");
        syntax.getCell(0, 3).setText("SPECS"); syntax.getCell(0, 4).setText("SPECS");
        syntax.getCell(1, 0).setText("T-1"); syntax.getCell(1, 1).setText("TP"); syntax.getCell(1, 2).setText("D16");
        syntax.getCell(1, 3).setText("HV"); syntax.getCell(1, 4).setText("750");
        syntax.getCell(2, 0).setText("T-1"); syntax.getCell(2, 1).setText("SN"); syntax.getCell(2, 2).setText("001");
        syntax.getCell(2, 3).setText("LV"); syntax.getCell(2, 4).setText("110");
        syntax.getCell(3, 0).setText("T-2"); syntax.getCell(3, 1).setText("TP"); syntax.getCell(3, 2).setText("D24");
        syntax.getCell(3, 3).setText("HV"); syntax.getCell(3, 4).setText("110");
        syntax.getCell(4, 0).setText("T-2"); syntax.getCell(4, 1).setText("SN"); syntax.getCell(4, 2).setText("002");
        syntax.getCell(4, 3).setText("LV"); syntax.getCell(4, 4).setText("10");

        CellDerivedItem[] identity = new CellDerivedItem[4];
        for (int i = 0; i < 4; i++) {
            identity[i] = new CellDerivedItem(syntax.getCell(i + 1, 0).text(), 0, syntax.getCell(i + 1, 0), ItemType.VALUE);
        }

        CellDerivedItem[][] data = new CellDerivedItem[4][2];
        for (int i = 0; i < 4; i++) {
            data[i][0] = new CellDerivedItem(syntax.getCell(i + 1, 2).text(), 0, syntax.getCell(i + 1, 2), ItemType.VALUE);
            data[i][1] = new CellDerivedItem(syntax.getCell(i + 1, 4).text(), 0, syntax.getCell(i + 1, 4), ItemType.VALUE);
        }

        CellDerivedItem[][] rowAttr = new CellDerivedItem[4][2];
        for (int i = 0; i < 4; i++) {
            rowAttr[i][0] = new CellDerivedItem(syntax.getCell(i + 1, 1).text(), 0, syntax.getCell(i + 1, 1), ItemType.ATTRIBUTE);
            rowAttr[i][1] = new CellDerivedItem(syntax.getCell(i + 1, 3).text(), 0, syntax.getCell(i + 1, 3), ItemType.ATTRIBUTE);
        }

        CellDerivedItem auxRef = new CellDerivedItem("REF", 0, syntax.getCell(0, 1), ItemType.AUXILIARY);
        CellDerivedItem auxSpecs = new CellDerivedItem("SPECS", 0, syntax.getCell(0, 3), ItemType.AUXILIARY);

        ContextDerivedItem betaId = new ContextDerivedItem("ID", ItemType.ATTRIBUTE);

        Set<CellDerivedItem> allCdi = new LinkedHashSet<>();
        allCdi.addAll(List.of(identity));
        for (var arr : data) allCdi.addAll(List.of(arr));
        for (var arr : rowAttr) allCdi.addAll(List.of(arr));
        allCdi.addAll(List.of(auxRef, auxSpecs));

        Set<ContextDerivedItem> allCtx = Set.of(betaId);

        List<InterpretationAction> actions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            CellDerivedItem targetAux0 = auxRef;
            CellDerivedItem targetAux1 = auxSpecs;

            actions.add(new InterpretationAction(rowAttr[i][0],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == targetAux0, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new PrefixOperation("_")));

            actions.add(new InterpretationAction(rowAttr[i][1],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == targetAux1, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new PrefixOperation("_")));
        }

        for (int i = 0; i < 4; i++) {
            actions.add(new InterpretationAction(identity[i],
                    List.of(new ContextDerivedItemProvider(List.of(betaId))),
                    new AvpOperation()));
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                CellDerivedItem targetAttr = rowAttr[i][j];
                actions.add(new InterpretationAction(data[i][j],
                        List.of(new CellDerivedItemProvider(
                                (a, cand) -> cand == targetAttr, TraversalOrder.ROW_MAJOR,
                                allCdi, 1)),
                        new AvpOperation()));
            }
        }

        for (int i = 0; i < 4; i++) {
            CellDerivedItem d0 = data[i][0];
            CellDerivedItem d1 = data[i][1];
            actions.add(new InterpretationAction(identity[i],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == d0 || cand == d1, TraversalOrder.COLUMN_MAJOR,
                            allCdi, Integer.MAX_VALUE)),
                    new RecOperation()));
        }

        CellDerivedItem target1 = identity[1];
        actions.add(new InterpretationAction(identity[0],
                List.of(new CellDerivedItemProvider(
                        (a, cand) -> cand == target1, TraversalOrder.ROW_MAJOR,
                        allCdi, 1)),
                new ConcatOperation()));

        CellDerivedItem target3 = identity[3];
        actions.add(new InterpretationAction(identity[2],
                List.of(new CellDerivedItemProvider(
                        (a, cand) -> cand == target3, TraversalOrder.ROW_MAJOR,
                        allCdi, 1)),
                new ConcatOperation()));

        TableSemantics semantics = new TableSemantics(allCdi, allCtx, actions);
        InterpretableTable itm = new InterpretableTable(syntax, semantics);

        Recordset result = new TableInterpreter().interpret(itm);

        assertEquals(2, result.size());

        Record t1 = result.records().stream()
                .filter(r -> "T-1".equals(r.get("ID")))
                .findFirst().orElseThrow();
        assertEquals("D16", t1.get("REF_TP"));
        assertEquals("001", t1.get("REF_SN"));
        assertEquals("750", t1.get("SPECS_HV"));
        assertEquals("110", t1.get("SPECS_LV"));

        Record t2 = result.records().stream()
                .filter(r -> "T-2".equals(r.get("ID")))
                .findFirst().orElseThrow();
        assertEquals("D24", t2.get("REF_TP"));
        assertEquals("002", t2.get("REF_SN"));
        assertEquals("110", t2.get("SPECS_HV"));
        assertEquals("10", t2.get("SPECS_LV"));
    }
}
