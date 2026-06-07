package ru.icc.regtab.rtl;

/**
 * Task 111: cross-tabulation with a unit header row, a subheader row, and data rows.
 * Each data row produces one record: the average (AVE) anchors REC collecting all
 * same-row items (INDICATOR, YEAR, MIN, MAX) via ROW* and the same-column unit via COL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_111/}
 * <pre>
 * [ []{2} [VAL: 'UNIT'-&gt;AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP] [VAL: 'YEAR'-&gt;AVP] [VAL: 'MIN'-&gt;AVP] [VAL: 'MAX'-&gt;AVP]
 *   [VAL: 'AVE'-&gt;AVP, (ROW*,COL)-&gt;REC] ]+
 * </pre>
 */
public class RtlTask111Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "111"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} [VAL: 'UNIT'->AVP]+ ]
                [ []+ ]
                [ [VAL: 'INDICATOR'->AVP] [VAL: 'YEAR'->AVP] [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
                  [VAL: 'AVE'->AVP, (ROW*,COL)->REC]
                ]+
                """;
    }
}
