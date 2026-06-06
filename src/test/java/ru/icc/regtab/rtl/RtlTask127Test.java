package ru.icc.regtab.rtl;

/**
 * Task 127: complex cross-tabulation with YEAR header, two-level INDICATOR subheader
 * (PREFIX from above), and two symmetric blocks of MIN-MAX&lt;br&gt;AVE data cells.
 * REC for the first block: (CL*,ROW&amp;C0..1,COL,COL&amp;R2,ROW&amp;C5).
 * REC for the second block: (CL*,ROW,ROW&amp;C6,COL,COL&amp;R2,ROW&amp;C10).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_127/}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [] { [] [VAL: -AV*-&gt;PREFIX, 'INDICATOR'-&gt;AVP]{3} [] }+ ]
 * [ [VAL: 'HYDROBIONT_GROUP'-&gt;AVP ',' VAL: 'UNIT'-&gt;AVP]
 *   [VAL: 'TIME'-&gt;AVP]
 *   [('\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '&lt;br&gt;'
 *                      VAL: 'AVE'-&gt;AVP, (CL*,ROW&amp;C0..1,COL,COL&amp;R2,ROW&amp;C5)-&gt;REC)]{3}
 *   [VAL: 'AREA'-&gt;AVP]
 *   [VAL: 'TIME'-&gt;AVP]
 *   [('\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '&lt;br&gt;'
 *                      VAL: 'AVE'-&gt;AVP, (CL*,ROW,ROW&amp;C6,COL,COL&amp;R2,ROW&amp;C10)-&gt;REC)]{3}
 *   [VAL: 'AREA'-&gt;AVP] ]+
 * </pre>
 */
public class RtlTask127Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "127"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]+ ]
                [ [AUX]+ ]
                [ [] { [] [VAL: -AV{1}->PREFIX, 'INDICATOR'->AVP]{3} [] }+ ]
                [ [VAL: 'HYDROBIONT_GROUP'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
                  [VAL: 'TIME'->AVP]
                  [('\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '<br>'
                                       VAL: 'AVE'->AVP, (CL*,ROW&C0..1*,COL,COL&R2,ROW&C5)->REC)]{3}
                  [VAL: 'AREA'->AVP]
                  [VAL: 'TIME'->AVP]
                  [('\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '<br>'
                                       VAL: 'AVE'->AVP, (CL*,ROW{2},ROW&C6,COL,COL&R2,ROW&C10)->REC)]{3}
                  [VAL: 'AREA'->AVP] ]+
                """;
    }
}
