package ru.icc.regtab.interpret;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.Diagnostic;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.rtl.RtlCompiler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end: anchors carrying several records after {@code JOIN} (the record product) flow
 * through schema construction (both strategies) and record generation; joined-away anchors
 * are excluded from the recordset and from recordset-consistency; {@code CONCAT} conflicts
 * surface through {@link TableInterpreter#diagnostics()} or, under strict preconditions,
 * as an exception.
 */
class TableInterpreterMultiRecordTest {

    /** id | x | y  /  a;b | 1 | 2  /  c | 3 | 4 — explode × stack, six records. */
    private static TableSyntax explodeStack() {
        TableSyntax s = new TableSyntax(3, 3);
        String[][] rows = {{"id", "x", "y"}, {"a;b", "1", "2"}, {"c", "3", "4"}};
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) s.getCell(r, c).setText(rows[r][c]);
        return s;
    }

    private static final String JOIN_PRODUCT = """
            [ [ATTR] [VAL: 'var'->AVP]+ ]
            [ [(VAL: COL->AVP, ()->REC, RT*->JOIN){';'}] [VAL: 'value'->AVP, COL->REC]+ ]+
            """;

    private static Recordset run(TableInterpreter interpreter, String rtl, TableSyntax syntax) {
        TablePattern pattern = RtlCompiler.compile(rtl);
        InterpretableTable itm = AtpMatcher.match(pattern, syntax).orElseThrow();
        return pattern.transform(interpreter.interpret(itm));
    }

    private static List<String> row(Record r, String... attrs) {
        return java.util.Arrays.stream(attrs).map(r::get).toList();
    }

    @Test
    void joinProduct_recordFirst() {
        TableInterpreter interpreter = new TableInterpreter();
        Recordset rs = run(interpreter, JOIN_PRODUCT, explodeStack());

        assertEquals(List.of("id", "value", "var"), rs.schema().attributes());
        assertEquals(6, rs.size());
        List<List<String>> rows = rs.records().stream().map(r -> row(r, "id", "var", "value")).toList();
        assertEquals(List.of(
                List.of("a", "x", "1"), List.of("a", "y", "2"),
                List.of("b", "x", "1"), List.of("b", "y", "2"),
                List.of("c", "x", "3"), List.of("c", "y", "4")), rows);
        assertTrue(interpreter.diagnostics().isEmpty());
    }

    @Test
    void joinProduct_positionFirst_sameRecords() {
        TableInterpreter interpreter = new TableInterpreter().withStrategy(SchemaConstructionStrategy.POSITION_FIRST);
        Recordset rs = run(interpreter, JOIN_PRODUCT, explodeStack());

        assertEquals(List.of("id", "value", "var"), rs.schema().attributes());
        assertEquals(6, rs.size());
        assertEquals(List.of("b", "y", "2"), row(rs.get(3), "id", "var", "value"));
    }

    /** k | v  /  A | 5  /  A | 7 — the two rows share the named attribute v: a CONCAT conflict. */
    private static TableSyntax conflict() {
        TableSyntax s = new TableSyntax(3, 2);
        String[][] rows = {{"k", "v"}, {"A", "5"}, {"A", "7"}};
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 2; c++) s.getCell(r, c).setText(rows[r][c]);
        return s;
    }

    private static final String CONCAT_CONFLICT = """
            [ [ATTR]+ ]
            [ [VAL: COL->AVP, RT->REC, BW&STR*->CONCAT(0)] [VAL: COL->AVP] ]+
            """;

    @Test
    void concatConflict_noEffect_bothRecordsSurvive_diagnosticReported() {
        TableInterpreter interpreter = new TableInterpreter();
        Recordset rs = run(interpreter, CONCAT_CONFLICT, conflict());

        assertEquals(2, rs.size(), "neither row is folded, nothing is lost silently");
        assertEquals(List.of("A", "5"), row(rs.get(0), "k", "v"));
        assertEquals(List.of("A", "7"), row(rs.get(1), "k", "v"));
        assertEquals(1, interpreter.diagnostics().size());
        Diagnostic d = interpreter.diagnostics().getFirst();
        assertEquals("CONCAT", d.operation());
        assertTrue(d.message().contains("'v'"), d.message());
    }

    @Test
    void concatConflict_strictPreconditions_throws() {
        TableInterpreter interpreter = new TableInterpreter().withStrictPreconditions(true);
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> run(interpreter, CONCAT_CONFLICT, conflict()));
        assertTrue(e.getMessage().contains("CONCAT"), e.getMessage());
    }

    @Test
    void diagnosticsAreResetPerInterpretation() {
        TableInterpreter interpreter = new TableInterpreter();
        run(interpreter, CONCAT_CONFLICT, conflict());
        assertEquals(1, interpreter.diagnostics().size());
        run(interpreter, JOIN_PRODUCT, explodeStack());
        assertTrue(interpreter.diagnostics().isEmpty());
    }
}
