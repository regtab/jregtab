package ru.icc.regtab.rtl;

/**
 * Task 133: tourism statistics table with location blocks and year columns.
 * Header row has any anchor cell then year values. Each subtable starts with a
 * LOCATION header row (VAL, then any cells), followed by INDICATOR rows.
 * REC on DATA collects INDICATOR (ROW), YEAR from col header (COL), and
 * LOCATION via ST — same-subtable scope picks up the location cell's AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_133/}
 * <pre>
 *   [ [] [VAL : 'YEAR'-&gt;AVP]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ [VAL : 'INDICATOR'-&gt;AVP] [!BLANK ? VAL : 'DATA'-&gt;AVP, (ROW,COL,ST)-&gt;REC]+ ]+ }+
 * </pre>
 */
public class RtlTask133Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "133"; }

    @Override
    protected String buildRtl() {
        return """
                  [ [] [VAL : 'YEAR'->AVP]+ ]
                { [ [VAL : 'LOCATION'->AVP] []+ ]
                  [ [VAL : 'INDICATOR'->AVP] [!BLANK ? VAL : 'DATA'->AVP, (ROW,COL,ST)->REC]+ ]+ }+
                """;
    }
}
