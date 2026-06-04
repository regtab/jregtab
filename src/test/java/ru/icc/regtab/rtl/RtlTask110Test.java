package ru.icc.regtab.rtl;

/**
 * Task 110: rows with non-blank anchor cells collecting right-of values via RT*->REC,
 * each group separated by an optional blank cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_110/}
 * <pre>
 * [ { [!BLANK ? VAL: RT*-&gt;REC] [!BLANK ? VAL]+ [BLANK]? }+ ]+
 * </pre>
 */
public class RtlTask110Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "110"; }

    @Override
    protected String buildRtl() {
        return """
                [ { [!BLANK ? VAL: RT*->REC] [!BLANK ? VAL]+ [BLANK]? }+ ]+
                """;
    }
}
