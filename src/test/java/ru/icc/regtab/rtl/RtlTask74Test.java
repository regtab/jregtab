package ru.icc.regtab.rtl;

/**
 * Task 74:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_74/}
 */
public class RtlTask74Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "74"; }

    @Override
    protected String buildRtl() {
        return """
                [          [ATTR]{3} ]
                [ COL->AVP [VAL: (RT*, @'D'='d')->REC][VAL]{2} ]+
                """;
    }
}
