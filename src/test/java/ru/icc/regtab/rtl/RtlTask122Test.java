package ru.icc.regtab.rtl;

/**
 * Task 122: cross-tabulation with YEAR/MONTH headers, explicit subtable pairing MIN-MAX row
 * with AVE row. SUBSTR(0,4) extracts 4-char year. condContSpec guards empty/dash-only cells.
 * REC on AVE collects INDICATOR at col 0 (ROW&amp;C0), MIN/MAX from row above (-AV{2}),
 * and YEAR/MONTH at rows 0-1 in same column (COL&amp;R0..1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_122/}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]+ ]
 * [ [] [VAL: 'MONTH'-&gt;AVP]+ ]
 * { [ [] [('\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP)]{6} [] ]
 *   [ [VAL: 'INDICATOR'-&gt;AVP]
 *     [('\s*-?\s*' ? _ | VAL: 'AVE'-&gt;AVP, (ROW&amp;C0,-AV{2},COL&amp;R0,COL&amp;R1)-&gt;REC)]{6} [] ] }+
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
                { [ [] [('\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP)]{6} [] ]
                  [ [VAL: 'INDICATOR'->AVP]
                    [('\\s*-?\\s*' ? _ | VAL: 'AVE'->AVP, (ROW,-AV{2},COL&R0,COL&R1)->REC)]{6} [] ] }+
                """;
    }
}
