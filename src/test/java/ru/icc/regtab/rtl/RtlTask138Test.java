package ru.icc.regtab.rtl;

/**
 * Task 138: SPNA visitor statistics with two-row header (attr names + years).
 * Row 1: first 2 cells are ATTR=UC headers (SPNA, NUMBER OF VISITORS), rest empty.
 * Row 2: empty anchor cell, then year values.
 * Data rows: SPNA name cell uses COL->AVP (col-0 header). Visitor count cells use
 * C1->AVP — absolute column 1 header ("NUMBER OF VISITORS") as attribute name for
 * all data columns. REC collects ROW (SPNA name) and COL (year).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_138/}
 * <pre>
 * [ [ATTR=UC]{2} []+ ]
 * [ [] [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [VAL : COL-&gt;AVP] [VAL : C1-&gt;AVP, (ROW,COL)-&gt;REC]+ ]+
 * </pre>
 */
public class RtlTask138Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "138"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [ATTR=UC]{2} []+ ]
                [ [] [VAL : 'YEAR'->AVP]+ ]
                [ [VAL : COL->AVP] [VAL : C1->AVP, (ROW,COL)->REC]+ ]+
                """;
    }
}
