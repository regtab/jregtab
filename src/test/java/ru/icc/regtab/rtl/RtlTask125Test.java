package ru.icc.regtab.rtl;

/**
 * Task 125: simple AVE-only cross-tabulation with MONTH header.
 * condContSpec skips empty/dash-only cells; otherwise assigns AVE.
 * REC collects INDICATOR at col 1 (ROW&amp;C1) and MONTH at row 0 (COL&amp;R0).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_125/}
 * <pre>
 * [ []{2} [VAL: 'MONTH'-&gt;AVP]+ ]
 * [ [] [VAL: 'INDICATOR'-&gt;AVP]
 *   ['\s*-?\s*' ? _ | VAL: 'AVE'-&gt;AVP, (ROW&amp;C1,COL&amp;R0)-&gt;REC]+ ]+
 * </pre>
 */
public class RtlTask125Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "125"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ []{2} [VAL: 'MONTH'->AVP]+ ]
                [ [] [VAL: 'INDICATOR'->AVP]
                  ['\\s*-?\\s*' ? _ | VAL: 'AVE'->AVP, (ROW&C1,COL)->REC]+ ]+
                """;
    }
}
