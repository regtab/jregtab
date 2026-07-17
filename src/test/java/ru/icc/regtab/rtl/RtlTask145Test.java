package ru.icc.regtab.rtl;

/**
 * Task 145: district tourism statistics with region blocks and year columns.
 * Row 1: first 2 cells are ATTR=UC (Districts, Indicators), rest are year values.
 * Each subtable starts with a LOCATION header row. Data rows use COL->AVP
 * (row-level actSpec). First column (district) may be blank and fills from above.
 * Second column (indicator) is always non-blank. Each of the 3 data cells
 * gets 'DATA'->AVP and creates REC via ROW{2} (district + indicator) and COL (year).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_145/}
 * <pre>
 *   [ [ATTR=UC]{2} [VAL : 'YEAR'-&gt;AVP]+ ]
 * { [ [VAL#'LOC' : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]
 *     [!BLANK ? VAL]
 *     [VAL : 'DATA'-&gt;AVP, (ROW{2},COL)-&gt;REC]{3} ]+ }+
 * </pre>
 */
public class RtlTask145Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "145"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                  [ [ATTR=UC]{2} [VAL : 'YEAR'->AVP]+ ]
                { [ [VAL#'LOC' : 'LOCATION'->AVP] []+ ]
                  [ COL->AVP
                    [BLANK ? VAL : -AV&!BLANK->FILL | VAL]
                    [!BLANK ? VAL]
                    [VAL : 'DATA'->AVP, (ROW{2},COL)->REC]{3} ]+ }+
                """;
    }
}
