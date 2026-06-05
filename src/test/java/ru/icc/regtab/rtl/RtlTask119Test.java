package ru.icc.regtab.rtl;

/**
 * Task 119: cross-tabulation with location header, plain indicator cell,
 * and blank AVE handling. REC on AVE collects same-row items at cols 0..1
 * (INDICATOR, YEAR), same-subrow left items at cols -2..-1 (MAX, MIN) in
 * reverse order, and same-column LOCATION.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_119/}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP]
 *   [VAL: 'YEAR'-&gt;AVP] { [VAL: 'MIN'-&gt;AVP] [VAL: 'MAX'-&gt;AVP]
 *   [(BLANK ? _ | VAL: 'AVE'-&gt;AVP, (ROW&amp;C0..1*, -LT&amp;C-2..-1*, COL)-&gt;REC)] }+ ]+
 * </pre>
 */
public class RtlTask119Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "119"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} [VAL: 'LOCATION'->AVP]+ ]
                [ []+ ]
                [ [VAL: 'INDICATOR'->AVP]
                  [VAL: 'YEAR'->AVP] { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
                  [(BLANK ? _ | VAL: 'AVE'->AVP, (ROW&C0..1*, -LT&C-2..-1*, COL)->REC)] }+ ]+
                """;
    }
}
