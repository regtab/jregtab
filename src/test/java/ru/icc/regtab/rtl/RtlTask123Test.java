package ru.icc.regtab.rtl;

/**
 * Task 123: cross-tabulation with compound YEAR\nMONTH header cell and
 * compound MIN-MAX\nAVE data cell. YEAR extracted with SUBSTR(0,4).
 * REC on AVE collects same-cell attributes (CL*), INDICATOR in same row (ROW),
 * and YEAR/MONTH from compound header cell in same column at row 1 (COL&amp;R1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_123/}
 * <pre>
 * [ []+ ]
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP '\n' VAL: 'MONTH'-&gt;AVP]{4} [] ]
 * [ [VAL: 'INDICATOR'-&gt;AVP]
 *   [VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '\n' VAL: 'AVE'-&gt;AVP, (CL*,ROW,COL&amp;R1*)-&gt;REC]{4} [] ]+
 * </pre>
 */
public class RtlTask123Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "123"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ []+ ]
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP '\\n' VAL: 'MONTH'->AVP]{4} [] ]
                [ [VAL: 'INDICATOR'->AVP]
                  [VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\\n' VAL: 'AVE'->AVP, (CL*,ROW,COL&R1*)->REC]{4} [] ]+
                """;
    }
}
