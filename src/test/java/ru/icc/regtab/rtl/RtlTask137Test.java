package ru.icc.regtab.rtl;

/**
 * Task 137: protected areas table with ecological zone blocks and fill.
 * Header row extracts ATTR names uppercased (=UC). Each subtable starts with
 * a LOCATION header row (VAL, then any cells). Data rows use COL->AVP (row-level
 * actSpec). Third column (subject) may be blank and uses -AV&amp;!BLANK->FILL.
 * REC anchor on NAME collects ST (location) and ROW* (all other row cells).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_137/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP
 *     [VAL : (ST,ROW*)-&gt;REC]
 *     [VAL]
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]
 *     [!BLANK ? VAL]{2} ]+ }+
 * </pre>
 */
public class RtlTask137Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "137"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR=UC]+ ]
                { [ [VAL : 'LOCATION'->AVP] []+ ]
                  [ COL->AVP
                    [VAL : (ST,ROW*)->REC]
                    [VAL]
                    [BLANK ? VAL : -AV&!BLANK->FILL | VAL]
                    [!BLANK ? VAL]{2} ]+ }+
                """;
    }
}
