package ru.icc.regtab.rtl;

/**
 * Task 112: cross-tabulation with location header row, subheader row, and data rows.
 * Each data row has a compound INDICATOR, UNIT cell, a YEAR, and groups of MIN/MAX/AVE
 * per location. Blank AVE cells (missing data) are skipped.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_112/}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP ',' VAL=TRIM: 'UNIT'-&gt;AVP]
 *   [VAL: 'YEAR'-&gt;AVP]
 *   { [VAL: 'MIN'-&gt;AVP] [VAL: 'MAX'-&gt;AVP]
 *     [BLANK ? _ | VAL: 'AVE'-&gt;AVP; (ROW&amp;C0..1*, -LT&amp;C-2..-1*, COL)-&gt;REC] }+ ]+
 * </pre>
 */
public class RtlTask112Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "112"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} [VAL: 'LOCATION'->AVP]+ ]
                [ []+ ]
                [ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
                  [VAL: 'YEAR'->AVP] { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
                  [(BLANK ? _ | VAL: 'AVE'->AVP, (ROW&C0..1*, -LT&C-2..-1*, COL)->REC)] }+ ]+
                """;
    }
}
