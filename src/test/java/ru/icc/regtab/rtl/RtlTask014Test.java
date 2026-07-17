package ru.icc.regtab.rtl;

/**
 * Task 14: repeated subtables with a non-blank header row (blank sentinel in C2)
 * and data rows combining two subtable-scoped column lookups with two same-row values.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_014/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask014Test}
 * <pre>
 * { [ [!BLANK ? VAL]{2} [BLANK] ]
 *   [ [!BLANK ? VAL]{2} [!BLANK ? VAL : (ST&C0, ST&C1, SR{2})->REC(4)] ]+ }+
 * </pre>
 * Header row: two non-blank VAL cells then an optional blank sentinel. Data
 * rows: two non-blank VAL prefix cells, then a non-blank anchor producing
 * REC(4) with providers ST&C0 (subtable col-0), ST&C1 (subtable col-1), and
 * SR{2} (2 same-subrow values).
 */
public class RtlTask014Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "014"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                { [ [!BLANK ? VAL]{2} [BLANK] ]
                  [ [!BLANK ? VAL]{2} [!BLANK ? VAL : (ST&C0, ST&C1, SR{2})->REC(4)] ]+ }+
                """;
    }
}
