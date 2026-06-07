package ru.icc.regtab.rtl;

/**
 * Task 118: cross-tabulation with location header, conditional compound indicator cell
 * (splits on &lt;br&gt; into INDICATOR + OBSERVATION when present), and blank AVE handling.
 * REC on AVE collects same-row items (ROW{3}: INDICATOR, OBSERVATION, YEAR),
 * same-subrow left items (−LT{2}: MAX, MIN), and same-column LOCATION (COL).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_118/}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [~'&lt;br&gt;' ? VAL: 'INDICATOR'-&gt;AVP '&lt;br&gt;' VAL: 'OBSERVATION'-&gt;AVP | VAL: 'INDICATOR'-&gt;AVP]
 *   [VAL: 'YEAR'-&gt;AVP] { [VAL: 'MIN'-&gt;AVP] [VAL: 'MAX'-&gt;AVP]
 *   [BLANK ? _ | VAL: 'AVE'-&gt;AVP, (ROW{3},-LT{2},COL)-&gt;REC] }+ ]+
 * </pre>
 */
public class RtlTask118Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "118"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} [VAL: 'LOCATION'->AVP]+ ]
                [ []+ ]
                [ [~'<br>' ? VAL: 'INDICATOR'->AVP '<br>' VAL: 'OBSERVATION'->AVP | VAL: 'INDICATOR'->AVP]
                  [VAL: 'YEAR'->AVP] { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
                  [BLANK ? _ | VAL: 'AVE'->AVP, (ROW{3},-LT{2},COL)->REC] }+ ]+
                """;
    }
}
