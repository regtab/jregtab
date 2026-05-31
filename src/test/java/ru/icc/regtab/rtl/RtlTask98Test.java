package ru.icc.regtab.rtl;

/**
 * Task 98 RTL test.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_98/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask98Test}
 */
public class RtlTask98Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "98"; }

    @Override
    protected String buildRtl() {
        return """
                [ []                                    []    [ATTR]+ ]
                [ [VAL: RT*->REC, (BW&STR)*->JOIN(0,1)] [VAL] [VAL: COL->AVP]{2} [VAL]+ ]+
                """;
    }
}
