package ru.icc.regtab.rtl;

/**
 * Task 45: flat table where each row has a non-blank anchor cell and a non-blank
 * delimited cell whose comma-separated values each reference same-subrow column 0.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_045/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask045Test}
 * <pre>
 * [ [!BLANK? VAL] [!BLANK? (VAL : (SR & C0)->REC(1)){','}] ]+
 * </pre>
 * Each data row: a non-blank plain VAL anchor, then a non-blank delimited cell
 * where each comma-separated token is a VAL with REC(1) using provider SR & C0
 * (same-subrow column 0), binding the row-key anchor to every delimited value.
 */
public class RtlTask045Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "045"; }

    @Override
    protected String buildRtl() {
        return """
                [ [!BLANK? VAL] [!BLANK? (VAL : (SR & C0)->REC(1)){','}] ]+
                """;
    }
}
