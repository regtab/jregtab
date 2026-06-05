package ru.icc.regtab.rtl;

/**
 * Task 113: emission table with attribute header row, year row (SUBSTR 0..4), and data rows.
 * Each emission cell anchors a REC collecting same-row items via ROW (cardinality 1
 * picks the pollutant at col 0 first in row-major order).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_113/}
 * <pre>
 * [ [ATTR]{8} []* ]
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]{7} []* ]
 * [ [VAL: 'POLLUTANT'-&gt;AVP] [VAL: 'EMISSION'-&gt;AVP, ROW-&gt;REC]{7} []* ]+
 * </pre>
 */
public class RtlTask113Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "113"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR]{8} []* ]
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{7} []* ]
                [ [VAL: 'POLLUTANT'->AVP] [VAL: 'EMISSION'->AVP, ROW->REC]{7} []* ]+
                """;
    }
}
