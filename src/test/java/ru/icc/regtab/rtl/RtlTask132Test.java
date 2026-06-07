package ru.icc.regtab.rtl;

/**
 * Task 132: pollutant deposition table with explicit subtables per pollutant.
 * Each subtable has a POLLUTANT/UNIT header row (tagged #IND/#UNIT) followed by LOCATION rows.
 * REC on VALUE collects LOCATION (ROW&amp;C0), YEAR from row 1 (COL&amp;R1),
 * POLLUTANT from same subtable at col 0 with tag #IND (ST&amp;C0&amp;#'IND'),
 * and UNIT from same subtable at col 1 with tag #UNIT (ST&amp;C1&amp;#'UNIT').
 * Note: -AV&amp;C0 does not work because AV requires sameSubcol(anchor), which conflicts with C0
 * when the anchor is at a different column. ST&amp;C0 uses sameSubtable scope instead.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_132/}
 * <pre>
 * [ []+ ]
 * [ []{2} [VAL: 'YEAR'-&gt;AVP]{2} []{2} ]
 * { [ [VAL#'IND': 'POLLUTANT'-&gt;AVP] [!BLANK ? VAL#'UNIT': 'UNIT'-&gt;AVP] []{4} ]
 *   [ [VAL: 'LOCATION'-&gt;AVP] [BLANK]
 *     [('\s*-?\s*' ? _ | VAL: 'VALUE'-&gt;AVP, (ROW&amp;C0,COL&amp;R1,ST&amp;C0&amp;#'IND',ST&amp;C1&amp;#'UNIT')-&gt;REC)]{2}
 *     []{2} ]+ }+
 * </pre>
 */
public class RtlTask132Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "132"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ []{2} [VAL: 'YEAR'->AVP]{2} []{2} ]
                { [ [VAL#'IND': 'POLLUTANT'->AVP] [!BLANK ? VAL#'UNIT': 'UNIT'->AVP] []{4} ]
                  [ [VAL: 'LOCATION'->AVP] [BLANK]
                    ['\\s*-?\\s*' ? _ | VAL: 'VALUE'->AVP, (ROW,COL&R1,ST&C0&#'IND',ST&C1&#'UNIT')->REC]{2}
                    []{2}
                  ]+
                }+
                """;
    }
}
