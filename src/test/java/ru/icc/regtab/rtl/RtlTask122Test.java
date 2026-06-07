package ru.icc.regtab.rtl;

/**
 * Task 122: cross-tabulation with YEAR/MONTH headers, explicit subtable pairing MIN-MAX row
 * with AVE row. SUBSTR(0,4) extracts 4-char year. condContSpec guards empty/dash-only cells.
 * REC on AVE collects INDICATOR at col 0 (ROW), MIN/MAX from row above (-AV{2}),
 * and YEAR/MONTH in same column (COL{2}).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_122/}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]+ ]
 * [ [] [VAL: 'MONTH'-&gt;AVP]+ ]
 * { [ [] ['\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP]{6} [] ]
 *   [ [VAL: 'INDICATOR'-&gt;AVP]
 *     ['\s*-?\s*' ? _ | VAL: 'AVE'-&gt;AVP, (ROW,-AV{2},COL{2})-&gt;REC]{6} [] ] }+
 * </pre>
 */
public class RtlTask122Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "122"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]+ ]
                [ [] [VAL: 'MONTH'->AVP]+ ]
                { [ [] ['\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP]{6} [] ]
                  [ [VAL: 'INDICATOR'->AVP]
                    ['\\s*-?\\s*' ? _ | VAL: 'AVE'->AVP, (ROW,-AV{2},COL{2})->REC]{6} [] ] }+
                """;
    }
}
