package ru.icc.regtab.rtl;

/**
 * Task 76:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_76/}
 */
public class RtlTask76Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "76"; }

    @Override
    protected String buildRtl() {
        return """
                  [ []         [VAL]+ ]
                { [ [VAL]      [BLANK?]+ ]
                  [ [VAL=TRIM] [!BLANK? VAL: (COL, (ST & C0), ROW)->REC]+ ]+ }+
                """;
    }
}
