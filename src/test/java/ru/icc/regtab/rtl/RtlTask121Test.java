package ru.icc.regtab.rtl;

/**
 * Task 121: monthly cross-tabulation with compound MIN-MAX&lt;br&gt;AVE cells.
 * condContSpec guards empty/dash-only cells (skip); otherwise parses compound MIN, MAX, AVE.
 * REC collects same-cell attributes (CL*), INDICATOR at col 1 (ROW&amp;C1),
 * and MONTH at same column (COL).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_121/}
 * <pre>
 * [ []{2} [VAL: 'MONTH'-&gt;AVP]+ ]
 * [ [] [VAL: 'INDICATOR'-&gt;AVP]
 *   [('\s*-?\s*' ? _ |
 *   VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '&lt;br&gt;' VAL: 'AVE'-&gt;AVP, (CL*,ROW&amp;C1,COL)-&gt;REC)]+ ]+
 * </pre>
 */
public class RtlTask121Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "121"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} [VAL: 'MONTH'->AVP]+ ]
                [ [] [VAL: 'INDICATOR'->AVP]
                  [('\\s*-?\\s*' ? _ |
                  VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '<br>' VAL: 'AVE'->AVP, (CL*,ROW&C1,COL)->REC)]+ ]+
                """;
    }
}
