package ru.icc.regtab.rtl;

/**
 * Task 92: rows with one label VAL followed by grouped value VALs separated by blank cells;
 * REC anchor collects all same-row VALs to the right (ROW &amp; C+1..) and the label VAL to
 * the left (LT).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_92/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask92Test}
 * <pre>
 * [ [VAL] [VAL: ((ROW &amp; C+1..)*, LT)-&gt;REC ] { [!BLANK? VAL]+ [BLANK?]? }+ ]+
 * </pre>
 */
public class RtlTask92Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "92"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL] [VAL: ((ROW & C+1..)*, LT)->REC ] { [!BLANK? VAL]+ [BLANK?]? }+ ]+
                """;
    }
}
