package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class for test tasks. Each task defines a pattern via {@link #buildItm(TableSyntax)}.
 * Input and expected are loaded from CSV files.
 */
public abstract class TaskBase {

    /**
     * Builds InterpretableTable from the given TableSyntax (pattern or imperative).
     */
    protected abstract InterpretableTable buildItm(TableSyntax syntax);

    /**
     * Table interpreter used in {@link #runVariant}. Override to add transformations or options.
     */
    protected TableInterpreter newTableInterpreter() {
        return new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST);
    }

    /**
     * Optional post-step after interpretation (e.g. {@link ru.icc.regtab.itm.interpret.WhitespaceNormalization},
     * {@link ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition}). Default: identity.
     */
    protected Recordset transformActual(Recordset actual) {
        return actual;
    }

    /**
     * Returns task ID, e.g. "01" for Task01. Derived from class name.
     */
    public final String taskId() {
        String name = getClass().getSimpleName();
        if (name.startsWith("Task")) {
            return name.substring(4);
        }
        throw new IllegalStateException("Task class name must start with Task: " + name);
    }

    /**
     * Runs the task for the given variant: loads input, builds ITM, interprets, asserts against expected.
     */
    public final void runVariant(Path tasksRoot, int variantId) throws IOException {
        String tid = taskId();
        Path taskDir = tasksRoot.resolve("task_" + tid);
        Path inputPath = taskDir.resolve("input_" + variantId + ".csv");
        Path expectedPath = taskDir.resolve("expected_" + variantId + ".csv");

        TableSyntax syntax = CsvTableLoader.load(inputPath);
        InterpretableTable itm = buildItm(syntax);
        Recordset actual = transformActual(newTableInterpreter().interpret(itm));
        if (Files.exists(expectedPath)) {
            RecordsetMatchOptions matchOpts = TaskMatchOptionsLoader.load(tasksRoot, tid);
            Recordset expected = matchOpts.expectedHasHeader()
                    ? CsvRecordsetLoader.load(expectedPath)
                    : CsvRecordsetLoader.load(expectedPath, actual.schema());
            RecordsetAssert.assertMatches(actual, expected, matchOpts);
        }
    }
}
