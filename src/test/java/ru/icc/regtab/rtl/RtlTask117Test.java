package ru.icc.regtab.rtl;

/**
 * Task 117: discharge table with two data-row groups — million m3 (row 2) and tons (rows 3+).
 * Each EMISSION cell anchors REC with a context UNIT literal, same-row POLLUTANT (ROW&amp;C1),
 * and same-column YEAR from the header row (COL&amp;R1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_117/}
 * <pre>
 * [ []+ ]
 * [ [] [ATTR] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]{5} [] ]
 * [ [] [VAL: 'POLLUTANT'-&gt;AVP]
 *   [VAL: 'EMISSION'-&gt;AVP, (ROW&amp;C1,COL&amp;R1,@'UNIT'='MLN M3')-&gt;REC]{5} [] ]
 * [ [] [VAL: 'POLLUTANT'-&gt;AVP]
 *   [VAL: 'EMISSION'-&gt;AVP, (ROW&amp;C1,COL&amp;R1,@'UNIT'='TONS')-&gt;REC]{5} [] ]+
 * </pre>
 */
public class RtlTask117Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "117"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [] [ATTR] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{5} [] ]
                [ [] [VAL: 'POLLUTANT'->AVP]
                  [VAL: 'EMISSION'->AVP, (ROW&C1,COL&R1,@'UNIT'='MLN M3')->REC]{5} [] ]
                [ [] [VAL: 'POLLUTANT'->AVP]
                  [VAL: 'EMISSION'->AVP, (ROW&C1,COL&R1,@'UNIT'='TONS')->REC]{5} [] ]+
                """;
    }
}
