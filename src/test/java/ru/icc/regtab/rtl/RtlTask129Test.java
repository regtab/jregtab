package ru.icc.regtab.rtl;

/**
 * Task 129: cross-tabulation with YEAR header and compound HYDROBIONT_GROUP,UNIT cell.
 * Guard '\s*-\s*' (mandatory dash) skips dash-only cells.
 * REC collects same-cell attributes (CL*), HYDROBIONT_GROUP/UNIT from col 1 (ROW&amp;C1*),
 * and YEAR from row 0 in same column (COL&amp;R0).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_129/}
 * <pre>
 * [ [] [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]+ ]
 * [ [] [VAL: 'HYDROBIONT_GROUP'-&gt;AVP ',' VAL=TRIM: 'UNIT'-&gt;AVP]
 *   [('\s*-\s*' ? _ | VAL: 'MIN'-&gt;AVP '-' VAL: 'MAX'-&gt;AVP '&lt;br&gt;' 
 *                    VAL: 'AVE'-&gt;AVP, (CL*,ROW&amp;C1*,COL&amp;R0)-&gt;REC)]+ ]+
 * </pre>
 */
public class RtlTask129Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "129"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]+ ]
                [ [] [VAL: 'HYDROBIONT_GROUP'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
                  [('\\s*-\\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '<br>' 
                                      VAL: 'AVE'->AVP, (CL*,ROW&C1*,COL&R0)->REC)]+ ]+
                """;
    }
}
