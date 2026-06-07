package ru.icc.regtab.rtl;

/**
 * Task 116: multi-level header table (territory/location/indicator rows) with yearly data.
 * Each VALUE cell anchors REC collecting: YEAR (same row via ROW), TERRITORY and LOCATION
 * (same column rows 1..3 via COL&amp;R1..3*), and INDICATOR tagged #IND from above (via -AV&amp;#'IND').
 * <p>
 * Location headers at row 3 are filled via -AV-&gt;PREFIX to concatenate sub-group names from row 2.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_116/}
 * <pre>
 * [ []+ ]
 * [ [] [VAL: 'TERRITORY'-&gt;AVP]+ ]
 * [ []+ ]
 * [ 'LOCATION'-&gt;AVP [] { [VAL: -AV-&gt;PREFIX]{4} [VAL] [] }{2} { [VAL: -AV-&gt;PREFIX]{2} [VAL] [] }? ]
 * { [ [VAL#'IND': 'INDICATOR'-&gt;AVP ',' VAL: 'UNIT'-&gt;AVP]+ ]
 *   [ ['20\\d\\d' ? VAL: 'YEAR'-&gt;AVP]
 *     { [VAL: 'VALUE'-&gt;AVP, (ROW, COL&amp;R1..3*, -AV&amp;#'IND')-&gt;REC]{5} [] }{2}
 *     { [VAL: 'VALUE'-&gt;AVP, (ROW, COL&amp;R1..3*, -AV&amp;#'IND')-&gt;REC]{3} [] }?
 *   ]+
 * }+
 * </pre>
 */
public class RtlTask116Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "116"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [] [VAL: 'TERRITORY'->AVP]+ ]
                [ [AUX]+ ]
                [ 'LOCATION'->AVP [] { [VAL: -AV{1}->PREFIX]{4} [VAL] [] }{2} { [VAL: -AV->PREFIX]{2} [VAL] [] }? ]
                { [ [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+ ]
                  [ ['20\\d\\d' ? VAL: 'YEAR'->AVP]
                    { [VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]{5} [] }{2}
                    { [VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]{3} [] }? 
                  ]+
                }+
                """;
    }
}
