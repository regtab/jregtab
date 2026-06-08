package ru.icc.regtab.rtl;

/**
 * Task 124: cross-tabulation with separate YEAR/MONTH header rows and compound
 * MIN-MAX\nAVE(IN_NW) data cells.
 * REC on AVE collects same-cell attributes (CL*), INDICATOR at col 0 (ROW),
 * and YEAR/MONTH in same column (COL{2}).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_124/}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]{6} []{2} ]
 * [ [] [VAL: 'MONTH'-&gt;AVP]{6} []{2} ]
 * [ [VAL: 'INDICATOR'-&gt;AVP]
 *   [VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '\n' VAL=TRIM: 'AVE'-&gt;AVP, 
 *   (CL*,ROW,COL{2})-&gt;REC '(' VAL: 'IN_NORTHWESTERN_SECTION'-&gt;AVP ')']{6}
 *   []{2} ]+
 * </pre>
 */
public class RtlTask124Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "124"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{6} []{2} ]
                [ [] [VAL: 'MONTH'->AVP]{6} []{2} ]
                [ [VAL: 'INDICATOR'->AVP]
                  [VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\\n' VAL=TRIM: 'AVE'->AVP, 
                  (CL*,ROW,COL{2})->REC '(' VAL: 'IN_NORTHWESTERN_SECTION'->AVP ')']{6}
                  []{2} ]+
                """;
    }
}
