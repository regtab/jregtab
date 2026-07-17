package ru.icc.regtab.rtl;

/**
 * Task 139: tourist routes visitor table with cluster fill and conditional REC.
 * Header row extracts ATTR names uppercased (=UC). Data rows use COL->AVP
 * (row-level actSpec). First column (cluster) may be blank — -AV&amp;!BLANK->FILL
 * inherits nearest non-blank value above. Third column (visitors) may be blank —
 * blank rows are skipped (_); non-blank rows create REC via ROW* (cluster + route).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_139/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP
 *   [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]
 *   [VAL]
 *   [BLANK ? _ | VAL : ROW*-&gt;REC] ]+
 * </pre>
 */
public class RtlTask139Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "139"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [ATTR=UC]+ ]
                [ COL->AVP
                  [BLANK ? VAL : -AV&!BLANK->FILL | VAL]
                  [VAL]
                  [BLANK ? _ | VAL : ROW*->REC] ]+
                """;
    }
}
