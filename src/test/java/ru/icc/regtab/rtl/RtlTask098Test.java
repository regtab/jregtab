package ru.icc.regtab.rtl;

/**
 * Task 98 RTL test.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_098/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask098Test}
 */
public class RtlTask098Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "098"; }

    @Override
    protected String buildRtl() {
        return """
                [ []                                    []    [ATTR]+ ]
                [ [VAL: RT*->REC, (BW&STR)*->JOIN(0,1)] [VAL] [VAL: COL->AVP]{2} [VAL]+ ]+
                """;
    }
}
