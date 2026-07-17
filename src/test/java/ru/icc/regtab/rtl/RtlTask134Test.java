package ru.icc.regtab.rtl;

/**
 * Task 134: tourism load table with month and day-count header rows, location blocks.
 * Two header rows: MONTH names, then day counts per month. Each subtable starts with
 * a LOCATION header row (VAL, then any cells), followed by INDICATOR rows.
 * REC on DATA collects INDICATOR (ROW), MONTH and DAY via COL{2} (both column headers),
 * and LOCATION via ST (same-subtable scope).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_134/}
 * <pre>
 *   [ [] [VAL : 'MONTH'-&gt;AVP]+ ]
 *   [ [] [VAL : 'DAY'-&gt;AVP]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ [VAL : 'INDICATOR'-&gt;AVP] [!BLANK ? VAL : 'DATA'-&gt;AVP, (ROW,COL{2},ST)-&gt;REC]+ ]+ }+
 * </pre>
 */
public class RtlTask134Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "134"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                  [ [] [VAL : 'MONTH'->AVP]+ ]
                  [ [] [VAL : 'DAY'->AVP]+ ]
                { [ [VAL : 'LOCATION'->AVP] []+ ]
                  [ [VAL : 'INDICATOR'->AVP] [!BLANK ? VAL : 'DATA'->AVP, (ROW,COL{2},ST)->REC]+ ]+ }+
                """;
    }
}
