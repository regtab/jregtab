package ru.icc.regtab.rtl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class RtlTaskBase {

    @TestFactory
    @DisplayName("RTL task matches Fluent API fixtures")
    final Stream<DynamicTest> taskVariants() {
        assumeTrue(!buildRtl().isBlank(), "RTL not yet implemented for task " + taskId());
        Path tasksRoot = Path.of("src/test/resources/tasks");
        Path taskDir = tasksRoot.resolve("task_" + taskId());
        return IntStream.rangeClosed(1, 9)
                .filter(i -> Files.exists(taskDir.resolve("input_" + i + ".csv")))
                .mapToObj(i -> DynamicTest.dynamicTest("variant_" + i,
                        () -> runVariant(i, taskDir, tasksRoot)));
    }

    private void runVariant(int variantId, Path taskDir, Path tasksRoot) throws Exception {
        TableSyntax syntax = CsvTableLoader.load(taskDir.resolve("input_" + variantId + ".csv"));

        var pattern = RtlCompiler.compile(buildRtl());
        InterpretableTable itm = AtpMatcher.match(pattern, syntax)
                .orElseThrow(() -> new AssertionError(
                        "RTL Task" + taskId() + " pattern did not match variant " + variantId));

        Recordset actual = pattern.transform(new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm));

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
