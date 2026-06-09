package ru.icc.regtab.rtl;

/**
 * Task 136: tourist-recreational areas table with location blocks and fill.
 * Header row extracts ATTR names uppercased (=UC). Each subtable starts with
 * a LOCATION header row (VAL, then any cells). Data rows use COL->AVP (row-level
 * actSpec) so attribute names come from column headers. First 2 columns may be
 * blank (hierarchical area/municipality) and use -AV&amp;!BLANK->FILL.
 * REC anchor on VAL collects ST (location) and ROW* (all remaining row cells).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_136/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]{2}
 *     [!BLANK ? VAL]
 *     [VAL : (ST,ROW*)-&gt;REC]
 *     [VAL]{4} ]+ }+
 * </pre>
 */
public class RtlTask136Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "136"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR=UC]+ ]
                { [ [VAL : 'LOCATION'->AVP] []+ ]
                  [ COL->AVP
                    [BLANK ? VAL : -AV&!BLANK->FILL | VAL]{2}
                    [!BLANK ? VAL]
                    [VAL : (ST,ROW*)->REC]
                    [VAL]{4} ]+ }+
                """;
    }
}
