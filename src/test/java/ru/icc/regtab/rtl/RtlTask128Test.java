package ru.icc.regtab.rtl;

/**
 * Task 128: cross-tabulation with LOCATION header and compound TIME YEAR cell.
 * condContSpec skips empty/dash-only cells; otherwise parses compound MIN-MAX&lt;br&gt;AVE.
 * REC collects same-cell attributes (CL*), TIME/YEAR from compound cell at col 1 (ROW&amp;C1*),
 * and LOCATION at same column (COL).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_128/}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'-&gt;AVP]+ ]
 * [ [VAL: 'HYDROBIONT_GROUP'-&gt;AVP] [VAL: 'TIME'-&gt;AVP ' ' VAL: 'YEAR'-&gt;AVP]
 *   [('\s*-?\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '&lt;br&gt;' 
 *                      VAL: 'AVE'-&gt;AVP, (CL*,ROW&amp;C1*,COL)-&gt;REC)]+ ]+
 * </pre>
 */
public class RtlTask128Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "128"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [] [VAL: 'LOCATION'->AVP]+ ]
                [ [VAL: 'HYDROBIONT_GROUP'->AVP] [VAL: 'TIME'->AVP ' ' VAL: 'YEAR'->AVP]
                  [('\\s*-?\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '<br>' 
                                       VAL: 'AVE'->AVP, (CL*,ROW&C1*,COL)->REC)]+ ]+
                """;
    }
}
