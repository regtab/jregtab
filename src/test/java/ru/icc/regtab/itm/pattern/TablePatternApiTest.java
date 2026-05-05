package ru.icc.regtab.itm.pattern;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for Table Pattern API using USAGE_EXAMPLE_1 scenario:
 * two subtables, anchor in first cell of each subtable, RecAction with sameSubtable predicate.
 */
class TablePatternApiTest {

    @Test
    void usageExample1_twoSubtablesWithRecAction() {
        // 1. TableSyntax: 4 rows × 7 cols, two subtables
        TableSyntax syntax = new TableSyntax(4, 7);
        syntax.defineSubtables(0, 2);

        // Subtable 1
        syntax.getCell(0, 0).setText("3200");
        syntax.getCell(0, 1).setText("906");
        syntax.getCell(0, 2).setText("AUST HOUSE & GARDEN");
        syntax.getCell(0, 3).setText("");
        syntax.getCell(0, 4).setText("");
        syntax.getCell(0, 5).setText("");
        syntax.getCell(0, 6).setText("");
        syntax.getCell(1, 0).setText("9-Jun");
        syntax.getCell(1, 1).setText("9-Jun");
        syntax.getCell(1, 2).setText("Covers Only");
        syntax.getCell(1, 3).setText("4.7385");
        syntax.getCell(1, 4).setText("1");
        syntax.getCell(1, 5).setText("* *");
        syntax.getCell(1, 6).setText("0");

        // Subtable 2
        syntax.getCell(2, 0).setText("3167");
        syntax.getCell(2, 1).setText("906");
        syntax.getCell(2, 2).setText("AUST PERSONAL COMPUTER");
        syntax.getCell(2, 3).setText("");
        syntax.getCell(2, 4).setText("");
        syntax.getCell(2, 5).setText("");
        syntax.getCell(2, 6).setText("");
        syntax.getCell(3, 0).setText("9-Jun");
        syntax.getCell(3, 1).setText("9-Jun");
        syntax.getCell(3, 2).setText("Covers Only");
        syntax.getCell(3, 3).setText("6.7839");
        syntax.getCell(3, 4).setText("3");
        syntax.getCell(3, 5).setText("* *");
        syntax.getCell(3, 6).setText("0");

        // 2. Table Pattern API
        InterpretableTable itm = TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec((a, c) -> c.is.in.sameSubtable(a))
                .cells().exactly(2).val()
                .cells().oneOrMore().skip()
                .rows().one()
                .cells().one().val()
                .cells().exactly(4).val()
                .cells().oneOrMore().skip()
                .apply(syntax);

        // 3. Interpret
        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);

        // 4. Assert
        assertEquals(2, result.size());

        // Record 1: 3200, 906, AUST HOUSE & GARDEN, 9-Jun, 9-Jun, Covers Only, 4.7385, 1 (8 values)
        assertEquals("3200", result.records().get(0).get("$a_1"));
        assertEquals("906", result.records().get(0).get("$a_2"));
        assertEquals("AUST HOUSE & GARDEN", result.records().get(0).get("$a_3"));
        assertEquals("9-Jun", result.records().get(0).get("$a_4"));
        assertEquals("9-Jun", result.records().get(0).get("$a_5"));
        assertEquals("Covers Only", result.records().get(0).get("$a_6"));
        assertEquals("4.7385", result.records().get(0).get("$a_7"));
        assertEquals("1", result.records().get(0).get("$a_8"));

        // Record 2: 3167, 906, AUST PERSONAL COMPUTER, 9-Jun, 9-Jun, Covers Only, 6.7839, 3
        assertEquals("3167", result.records().get(1).get("$a_1"));
        assertEquals("906", result.records().get(1).get("$a_2"));
        assertEquals("AUST PERSONAL COMPUTER", result.records().get(1).get("$a_3"));
        assertEquals("9-Jun", result.records().get(1).get("$a_4"));
        assertEquals("9-Jun", result.records().get(1).get("$a_5"));
        assertEquals("Covers Only", result.records().get(1).get("$a_6"));
        assertEquals("6.7839", result.records().get(1).get("$a_7"));
        assertEquals("3", result.records().get(1).get("$a_8"));
    }

    @Test
    void usageExample1_inferredSubtables() {
        // Same data as above, but WITHOUT defineSubtables — pattern infers subtable boundaries
        TableSyntax syntax = new TableSyntax(4, 7);
        // syntax.defineSubtables(0, 2) — NOT called

        syntax.getCell(0, 0).setText("3200");
        syntax.getCell(0, 1).setText("906");
        syntax.getCell(0, 2).setText("AUST HOUSE & GARDEN");
        syntax.getCell(0, 3).setText("");
        syntax.getCell(0, 4).setText("");
        syntax.getCell(0, 5).setText("");
        syntax.getCell(0, 6).setText("");
        syntax.getCell(1, 0).setText("9-Jun");
        syntax.getCell(1, 1).setText("9-Jun");
        syntax.getCell(1, 2).setText("Covers Only");
        syntax.getCell(1, 3).setText("4.7385");
        syntax.getCell(1, 4).setText("1");
        syntax.getCell(1, 5).setText("* *");
        syntax.getCell(1, 6).setText("0");

        syntax.getCell(2, 0).setText("3167");
        syntax.getCell(2, 1).setText("906");
        syntax.getCell(2, 2).setText("AUST PERSONAL COMPUTER");
        syntax.getCell(2, 3).setText("");
        syntax.getCell(2, 4).setText("");
        syntax.getCell(2, 5).setText("");
        syntax.getCell(2, 6).setText("");
        syntax.getCell(3, 0).setText("9-Jun");
        syntax.getCell(3, 1).setText("9-Jun");
        syntax.getCell(3, 2).setText("Covers Only");
        syntax.getCell(3, 3).setText("6.7839");
        syntax.getCell(3, 4).setText("3");
        syntax.getCell(3, 5).setText("* *");
        syntax.getCell(3, 6).setText("0");

        InterpretableTable itm = TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec((a, c) -> c.is.in.sameSubtable(a))
                .cells().exactly(2).val()
                .cells().oneOrMore().skip()
                .rows().one()
                .cells().one().val()
                .cells().exactly(4).val()
                .cells().oneOrMore().skip()
                .apply(syntax);

        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);

        assertEquals(2, result.size());
        assertEquals("3200", result.records().get(0).get("$a_1"));
        assertEquals("3167", result.records().get(1).get("$a_1"));
    }

    @Test
    void rec_multipleProviders_secondEmpty_sameAsSingleProvider() {
        TableSyntax syntax = new TableSyntax(4, 7);
        syntax.defineSubtables(0, 2);
        fillUsageExample1Cells(syntax);

        InterpretableTable itm = TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(
                        ProviderSpec.of((a, c) -> c.is.in.sameSubtable(a)),
                        ProviderSpec.of((a, c) -> false))
                .cells().exactly(2).val()
                .cells().oneOrMore().skip()
                .rows().one()
                .cells().one().val()
                .cells().exactly(4).val()
                .cells().oneOrMore().skip()
                .apply(syntax);

        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);

        assertEquals(2, result.size());
        assertEquals("3200", result.records().get(0).get("$a_1"));
        assertEquals("4.7385", result.records().get(0).get("$a_7"));
        assertEquals("1", result.records().get(0).get("$a_8"));
        assertEquals("3167", result.records().get(1).get("$a_1"));
    }

    @Test
    void singleCellWhenSkipOtherwiseSupportsValueAndCompoundBranches() {
        TableSyntax syntax = new TableSyntax(2, 2);
        syntax.getCell(0, 0).setText("group");
        syntax.getCell(0, 1).setText("name:alpha");
        syntax.getCell(1, 0).setText("");
        syntax.getCell(1, 1).setText("kind:beta");

        InterpretableTable itm = TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one()
                .when(cell -> cell.text() == null || cell.text().isBlank()).skip()
                .otherwise().val()
                .actions().rec((a, c) -> c.is.in.sameSubtable(a) && c.is.in.col(1))
                .cells().one()
                .when(cell -> cell.text() == null || cell.text().isBlank()).skip()
                .otherwise().attr()
                .sep(":")
                .val()
                .actions().avp((a, c) -> c.is.in.sameCell(a))
                .apply(syntax);

        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);

        assertNotNull(itm);
        assertEquals(1, result.size());
    }

    private static void fillUsageExample1Cells(TableSyntax syntax) {
        syntax.getCell(0, 0).setText("3200");
        syntax.getCell(0, 1).setText("906");
        syntax.getCell(0, 2).setText("AUST HOUSE & GARDEN");
        syntax.getCell(0, 3).setText("");
        syntax.getCell(0, 4).setText("");
        syntax.getCell(0, 5).setText("");
        syntax.getCell(0, 6).setText("");
        syntax.getCell(1, 0).setText("9-Jun");
        syntax.getCell(1, 1).setText("9-Jun");
        syntax.getCell(1, 2).setText("Covers Only");
        syntax.getCell(1, 3).setText("4.7385");
        syntax.getCell(1, 4).setText("1");
        syntax.getCell(1, 5).setText("* *");
        syntax.getCell(1, 6).setText("0");

        syntax.getCell(2, 0).setText("3167");
        syntax.getCell(2, 1).setText("906");
        syntax.getCell(2, 2).setText("AUST PERSONAL COMPUTER");
        syntax.getCell(2, 3).setText("");
        syntax.getCell(2, 4).setText("");
        syntax.getCell(2, 5).setText("");
        syntax.getCell(2, 6).setText("");
        syntax.getCell(3, 0).setText("9-Jun");
        syntax.getCell(3, 1).setText("9-Jun");
        syntax.getCell(3, 2).setText("Covers Only");
        syntax.getCell(3, 3).setText("6.7839");
        syntax.getCell(3, 4).setText("3");
        syntax.getCell(3, 5).setText("* *");
        syntax.getCell(3, 6).setText("0");
    }
}
