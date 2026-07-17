package ru.icc.regtab.rtl;

/**
 * Task 126: simple table with compound MIN-MAX\nAVE cells, no header rows.
 * condContSpec skips empty/dash-only cells; otherwise parses compound MIN, MAX, AVE.
 * REC collects same-cell attributes (CL) and INDICATOR at col 1 (ROW&amp;C1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_126/}
 * <pre>
 * [ [] [VAL: 'INDICATOR'-&gt;AVP]
 *   ['\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '\n' VAL: 'AVE'-&gt;AVP, (CL*,ROW&amp;C1)-&gt;REC]+ ]+
 * </pre>
 */
public class RtlTask126Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "126"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [] [VAL: 'INDICATOR'->AVP]
                  ['\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\\n' VAL: 'AVE'->AVP, (CL*,ROW&C1)->REC]+ ]+
                """;
    }
}
