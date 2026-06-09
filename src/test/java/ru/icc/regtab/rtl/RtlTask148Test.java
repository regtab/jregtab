package ru.icc.regtab.rtl;

/**
 * Task 148: infrastructure objects table with region blocks.
 * Header row extracts ATTR names uppercased (=UC). Each subtable starts with
 * a LOCATION header row (VAL, then any cells). Data rows use
 * COL->AVP (row-level actSpec). First cell is the REC anchor collecting
 * ROW* (other row cells) and ST (location header); remaining cells must be
 * non-blank.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_148/}
 * <pre>
 *   [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [VAL : (ROW*,ST)-&gt;REC] [!BLANK ? VAL]+ ]+ }+
 * </pre>
 */
public class RtlTask148Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "148"; }

    @Override
    protected String buildRtl() {
        return """
                  [ [ATTR=UC]+ ]
                { [ [VAL : 'LOCATION'->AVP] []+ ]
                  [ COL->AVP [VAL : (ROW*,ST)->REC] [!BLANK ? VAL]+ ]+ }+
                """;
    }
}
