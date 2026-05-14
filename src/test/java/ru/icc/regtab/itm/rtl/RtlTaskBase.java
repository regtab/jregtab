package ru.icc.regtab.itm.rtl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.atp.AtpMatcher;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.tasks.CsvRecordsetLoader;
import ru.icc.regtab.itm.tasks.CsvTableLoader;
import ru.icc.regtab.itm.tasks.RecordsetAssert;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class RtlTaskBase {

    @ParameterizedTest(name = "variant_{0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("RTL task matches Fluent API fixtures")
    final void runTaskVariants(int variantId) throws IOException {
        Path taskDir = Path.of("src/test/resources/tasks/task_" + taskId());
        TableSyntax syntax = CsvTableLoader.load(taskDir.resolve("input_" + variantId + ".csv"));

        TablePattern pattern = RtlCompiler.compile(buildRtl());
        InterpretableTable itm = AtpMatcher.match(pattern, syntax)
                .orElseThrow(() -> new AssertionError(
                        "RTL Task" + taskId() + " pattern did not match variant " + variantId));

        Recordset actual = transformActual(new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm));

        Recordset expected = CsvRecordsetLoader.load(taskDir.resolve("expected_" + variantId + ".csv"));
        RecordsetAssert.assertMatches(actual, expected);
        assertTrue(actual.size() > 0);
    }

    protected abstract String taskId();

    protected abstract String buildRtl();

    protected Recordset transformActual(Recordset actual) {
        return actual;
    }
}
