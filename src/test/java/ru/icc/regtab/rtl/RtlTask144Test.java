package ru.icc.regtab.rtl;

/**
 * Task 144: land plot table — no header row, all columns auto-named ATTR0-N.
 * First cell of each row is the REC anchor collecting all other row cells via
 * ROW*. Remaining cells may be blank and fill from the nearest cell above via
 * -AV&!BLANK->FILL (reverseRowMajor + ABOVE = nearest cell above in same subcolumn).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_144/}
 * <pre>
 * [ [VAL : ROW*-&gt;REC] [BLANK ? VAL : -AV-&gt;FILL | VAL]+ ]+
 * </pre>
 */
public class RtlTask144Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "144"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT*->REC] [BLANK ? VAL : -AV&!BLANK->FILL | VAL]+ ]+
                """;
    }
}
