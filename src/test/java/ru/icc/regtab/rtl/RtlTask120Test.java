package ru.icc.regtab.rtl;

/**
 * Task 120: cross-tabulation with location header, compound indicator/unit cell (no year),
 * and fully populated AVE cells (no blank handling needed).
 * REC on AVE collects same-row INDICATOR/UNIT (ROW{2}), same-subrow left items MIN/MAX
 * (-LT{2}), and same-column LOCATION (COL).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_120/}
 * <pre>
 * [ [] [VAL: 'LOCATION'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP ',' VAL=TRIM: 'UNIT'-&gt;AVP]
 *   { [VAL: 'MIN'-&gt;AVP] [VAL: 'MAX'-&gt;AVP]
 *     [VAL: 'AVE'-&gt;AVP, (ROW{2},-LT{2},COL)-&gt;REC] }+ ]+
 * </pre>
 */
public class RtlTask120Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "120"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [] [VAL: 'LOCATION'->AVP]+ ]
                [ []+ ]
                [ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
                  { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
                    [VAL: 'AVE'->AVP, (ROW{2},-LT{2},COL)->REC] }+ ]+
                """;
    }
}
