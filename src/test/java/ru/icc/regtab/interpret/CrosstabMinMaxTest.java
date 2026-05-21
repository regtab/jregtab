package ru.icc.regtab.interpret;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.*;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.*;
import ru.icc.regtab.itm.syntax.*;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduces Example 1 from the paper: crosstab with min/max values.
 *
 * Table:
 *           | Jan                       | Jul
 * Australia | 25 (min) -- 28 (max)      | 27 (min) -- 32 (max)
 * Austria   | 32 (min) -- 34 (max)      | 35 (min) -- 37 (max)
 *
 * Expected schema: S = <MIN, MAX, COUNTRY, MONTH, YEAR>
 */
class CrosstabMinMaxTest {

    @Test
    void testCrosstabMinMax() {
        TableSyntax syntax = new TableSyntax(3, 3);
        syntax.getCell(0, 0).setText("");
        syntax.getCell(0, 1).setText("Jan");
        syntax.getCell(0, 2).setText("Jul");
        syntax.getCell(1, 0).setText("Australia");
        syntax.getCell(1, 1).setText("25 (min) -- 28 (max)");
        syntax.getCell(1, 2).setText("27 (min) -- 32 (max)");
        syntax.getCell(2, 0).setText("Austria");
        syntax.getCell(2, 1).setText("32 (min) -- 34 (max)");
        syntax.getCell(2, 2).setText("35 (min) -- 37 (max)");

        Map<String, CellDerivedItem> dataVal = new LinkedHashMap<>();
        Map<String, CellDerivedItem> dataAttr = new LinkedHashMap<>();

        String[][] minVals = {{"25", "28"}, {"27", "32"}, {"32", "34"}, {"35", "37"}};
        int[][] dataPositions = {{1,1}, {1,2}, {2,1}, {2,2}};

        for (int d = 0; d < 4; d++) {
            int r = dataPositions[d][0], c = dataPositions[d][1];
            Cell cell = syntax.getCell(r, c);
            String key = r + "," + c;

            dataVal.put(key + ":0", new CellDerivedItem(minVals[d][0], 0, cell, ItemType.VALUE));
            dataAttr.put(key + ":1", new CellDerivedItem("MIN", 1, cell, ItemType.ATTRIBUTE));
            dataVal.put(key + ":2", new CellDerivedItem(minVals[d][1], 2, cell, ItemType.VALUE));
            dataAttr.put(key + ":3", new CellDerivedItem("MAX", 3, cell, ItemType.ATTRIBUTE));
        }

        CellDerivedItem iotaJan = new CellDerivedItem("Jan", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem iotaJul = new CellDerivedItem("Jul", 0, syntax.getCell(0, 2), ItemType.VALUE);
        CellDerivedItem iotaAustralia = new CellDerivedItem("Australia", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem iotaAustria = new CellDerivedItem("Austria", 0, syntax.getCell(2, 0), ItemType.VALUE);

        ContextDerivedItem betaCountry = new ContextDerivedItem("COUNTRY", ItemType.ATTRIBUTE);
        ContextDerivedItem betaMonth = new ContextDerivedItem("MONTH", ItemType.ATTRIBUTE);
        ContextDerivedItem betaYear = new ContextDerivedItem("YEAR", ItemType.ATTRIBUTE);
        ContextDerivedItem gammaYear = new ContextDerivedItem("2025", ItemType.VALUE);

        Set<CellDerivedItem> allCdi = new LinkedHashSet<>();
        allCdi.addAll(dataVal.values());
        allCdi.addAll(dataAttr.values());
        allCdi.addAll(List.of(iotaJan, iotaJul, iotaAustralia, iotaAustria));

        Set<ContextDerivedItem> allCtx = new LinkedHashSet<>(
                List.of(betaCountry, betaMonth, betaYear, gammaYear));

        List<InterpretationAction> actions = new ArrayList<>();

        for (int d = 0; d < 4; d++) {
            int r = dataPositions[d][0], c = dataPositions[d][1];
            String key = r + "," + c;

            CellDerivedItem minItem = dataVal.get(key + ":0");
            CellDerivedItem minAttr = dataAttr.get(key + ":1");
            CellDerivedItem maxItem = dataVal.get(key + ":2");
            CellDerivedItem maxAttr = dataAttr.get(key + ":3");

            actions.add(new InterpretationAction(minItem,
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == minAttr, TraversalOrder.ROW_MAJOR,
                            Set.copyOf(dataAttr.values()), 1)),
                    new AvpOperation()));

            actions.add(new InterpretationAction(maxItem,
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == maxAttr, TraversalOrder.ROW_MAJOR,
                            Set.copyOf(dataAttr.values()), 1)),
                    new AvpOperation()));
        }

        actions.add(new InterpretationAction(iotaAustralia,
                List.of(new ContextDerivedItemProvider(List.of(betaCountry))),
                new AvpOperation()));
        actions.add(new InterpretationAction(iotaAustria,
                List.of(new ContextDerivedItemProvider(List.of(betaCountry))),
                new AvpOperation()));

        actions.add(new InterpretationAction(iotaJan,
                List.of(new ContextDerivedItemProvider(List.of(betaMonth))),
                new AvpOperation()));
        actions.add(new InterpretationAction(iotaJul,
                List.of(new ContextDerivedItemProvider(List.of(betaMonth))),
                new AvpOperation()));

        actions.add(new InterpretationAction(gammaYear,
                List.of(new ContextDerivedItemProvider(List.of(betaYear))),
                new AvpOperation()));

        for (int d = 0; d < 4; d++) {
            int r = dataPositions[d][0], c = dataPositions[d][1];
            String key = r + "," + c;
            CellDerivedItem minItem = dataVal.get(key + ":0");
            CellDerivedItem maxItem = dataVal.get(key + ":2");
            CellDerivedItem rowHeader = (r == 1) ? iotaAustralia : iotaAustria;
            CellDerivedItem colHeader = (c == 1) ? iotaJan : iotaJul;

            actions.add(new InterpretationAction(minItem,
                    List.of(
                            new CellDerivedItemProvider(
                                    (a, cand) -> cand == maxItem, TraversalOrder.ROW_MAJOR,
                                    allCdi, 1),
                            new CellDerivedItemProvider(
                                    (a, cand) -> cand == rowHeader, TraversalOrder.ROW_MAJOR,
                                    allCdi, 1),
                            new CellDerivedItemProvider(
                                    (a, cand) -> cand == colHeader, TraversalOrder.ROW_MAJOR,
                                    allCdi, 1),
                            new ContextDerivedItemProvider(List.of(gammaYear))
                    ),
                    new RecOperation()));
        }

        TableSemantics semantics = new TableSemantics(allCdi, allCtx, actions);
        InterpretableTable itm = new InterpretableTable(syntax, semantics);

        Recordset result = new TableInterpreter().interpret(itm);

        assertEquals(4, result.size());
        List<String> schema = result.schema().attributes();
        assertTrue(schema.contains("MIN"));
        assertTrue(schema.contains("MAX"));
        assertTrue(schema.contains("COUNTRY"));
        assertTrue(schema.contains("MONTH"));
        assertTrue(schema.contains("YEAR"));

        for (Record rec : result.records()) {
            assertEquals("2025", rec.get("YEAR"));
        }

        Record ausJan = result.records().stream()
                .filter(r -> "Australia".equals(r.get("COUNTRY")) && "Jan".equals(r.get("MONTH")))
                .findFirst().orElseThrow();
        assertEquals("25", ausJan.get("MIN"));
        assertEquals("28", ausJan.get("MAX"));

        Record autJul = result.records().stream()
                .filter(r -> "Austria".equals(r.get("COUNTRY")) && "Jul".equals(r.get("MONTH")))
                .findFirst().orElseThrow();
        assertEquals("35", autJul.get("MIN"));
        assertEquals("37", autJul.get("MAX"));
    }
}
