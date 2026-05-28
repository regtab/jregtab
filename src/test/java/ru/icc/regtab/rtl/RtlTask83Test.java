package ru.icc.regtab.rtl;

/**
 * Task 83:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_83/}
 */
public class RtlTask83Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "83"; }

    @Override
    protected String buildRtl() {
        return """
                  [          [ATTR]+ ]
                { [ COL->AVP [VAL: (RT*, BW)->REC][VAL]+ ]
                  [          [VAL: 'D'->AVP ][]+ ] }+
                """;
    }
}
