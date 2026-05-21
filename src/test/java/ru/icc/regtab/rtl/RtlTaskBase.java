package ru.icc.regtab.rtl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.tasks.CsvRecordsetLoader;
import ru.icc.regtab.tasks.CsvTableLoader;
import ru.icc.regtab.tasks.RecordsetAssert;
import ru.icc.regtab.tasks.RecordsetMatchOptions;
import ru.icc.regtab.tasks.TaskMatchOptionsLoader;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class RtlTaskBase {

    @ParameterizedTest(name = "variant_{0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("RTL task matches Fluent API fixtures")
    final void runTaskVariants(int variantId) throws IOException {
        Path taskDir = Path.of("src/test/resources/tasks/task_" + taskId());
        TableSyntax syntax = CsvTableLoader.load(taskDir.resolve("input_" + variantId + ".csv"));

        var pattern = RtlCompiler.compile(buildRtl());
        InterpretableTable itm = AtpMatcher.match(pattern, syntax)
                .orElseThrow(() -> new AssertionError(
                        "RTL Task" + taskId() + " pattern did not match variant " + variantId));

        Recordset actual = pattern.transform(new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm));

        Path tasksRoot = Path.of("src/test/resources/tasks");
        RecordsetMatchOptions matchOpts = TaskMatchOptionsLoader.load(tasksRoot, taskId());
        Path expectedPath = taskDir.resolve("expected_" + variantId + ".csv");
        Recordset expected = matchOpts.expectedHasHeader()
                ? CsvRecordsetLoader.load(expectedPath)
                : CsvRecordsetLoader.load(expectedPath, actual.schema());
        RecordsetAssert.assertMatches(actual, expected, matchOpts);
        assertTrue(actual.size() > 0);
    }

    public final ru.icc.regtab.atp.spec.TablePattern buildPattern() {
        return RtlCompiler.compile(buildRtl());
    }

    protected abstract String taskId();

    protected abstract String buildRtl();
}
