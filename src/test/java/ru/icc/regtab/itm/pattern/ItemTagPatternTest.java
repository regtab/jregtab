package ru.icc.regtab.itm.pattern;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.tasks.CsvTableLoader;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTagPatternTest {

    @Test
    void setTag_attachesTagToCellDerivedItem() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setText("x");

        InterpretableTable itm = TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().val().setTag("#L1")
                .apply(syntax);

        long tagged = itm.semantics().cellDerivedItems().stream()
                .filter(c -> c.tags().contains("#L1"))
                .count();
        assertEquals(1, tagged);
    }

    @Test
    void task02_pattern_tagsHeaderCells() throws Exception {
        Path p = Path.of("src/test/resources/tasks/task_02/input_1.csv");
        TableSyntax syntax = CsvTableLoader.load(p);
        InterpretableTable itm = TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().check(c -> !c.textBlank()).val().setTag("#L1")
                .cells().one().skip()
                .rows().one()
                .cells().one().check(c -> !c.textBlank()).val().setTag("#L2")
                .cells().one().skip()
                .rows().oneOrMore()
                .cells().one().check(c -> !c.textBlank()).val()
                .actions().rec(
                        ProviderSpec.of((a, c) -> c.hasTag("#L1") || c.hasTag("#L2")),
                        ProviderSpec.of((a, c) -> c.sameRow(a), 1))
                .cells().one().val()
                .rows().zeroOrMore()
                .cells().one().skip()
                .cells().one().skip()
                .apply(syntax);
        long l1 = itm.semantics().cellDerivedItems().stream()
                .filter(c -> c.tags().contains("#L1"))
                .count();
        long l2 = itm.semantics().cellDerivedItems().stream()
                .filter(c -> c.tags().contains("#L2"))
                .count();
        assertTrue(l1 > 0, "expected #L1 tags on header cells");
        assertTrue(l2 > 0, "expected #L2 tags on header cells");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);
        assertTrue(rs.size() > 0, "expected non-empty recordset from Task02-like pattern");
        assertTrue(rs.schema().attributes().size() >= 2, "expected multi-column schema");
    }
}
