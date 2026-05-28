package ru.icc.regtab.rtl;

/**
 * Task 82:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_82/}
 */
public class RtlTask82Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "82"; }

    @Override
    protected String buildRtl() {
        return """
                { SC->AVP
                  [ [ATTR]+ ]
                  [ [!BLANK? VAL: RT*->REC] [VAL]+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
