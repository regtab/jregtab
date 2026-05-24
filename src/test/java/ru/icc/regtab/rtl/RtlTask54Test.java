package ru.icc.regtab.rtl;

/**
 * Task 54:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_54/}
 */
public class RtlTask54Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "54"; }

    @Override
    protected String buildRtl() {
        return """
                [ { []    [!BLANK? VAL]+               [BLANK?]? }+ ]
                [ { [VAL] [!BLANK? VAL: (SC,SR)->REC]+ [BLANK?]? }+ ]+
                """;
    }
}
