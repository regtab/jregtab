package ru.icc.regtab.rtl;

/**
 * Task 63:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_63/}
 */
public class RtlTask63Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "63"; }

    @Override
    protected String buildRtl() {
        return """
                [ -COL->AVP [VAL: RT*->REC] [VAL]* ]+
                [           [ATTR]+ ]
                """;
    }
}
