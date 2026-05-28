package ru.icc.regtab.rtl;

/**
 * Task 72:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_72/}
 */
public class RtlTask72Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "72"; }

    @Override
    protected String buildRtl() {
        return """
                [          [(BLANK? _ | ATTR)]+ ]
                [ COL->AVP [(BLANK? _ | VAL: RT*->REC)] [(BLANK? _ | VAL)]+ ]+
                """;
    }
}
