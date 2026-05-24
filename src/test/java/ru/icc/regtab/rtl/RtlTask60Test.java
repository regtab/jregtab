package ru.icc.regtab.rtl;

/**
 * Task 60:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_60/}
 */
public class RtlTask60Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "60"; }

    @Override
    protected String buildRtl() {
                return """
                  [ [ATTR]+ ]
                { COL->AVP
                  [ [!BLANK? VAL: RT*->REC] [(BLANK? VAL: -^(AV & !BLANK)->FILL | VAL)]+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
