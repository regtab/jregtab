package ru.icc.regtab.rtl;

/**
 * Task 140: protected areas visitor statistics — flat table, no fill needed.
 * Header row extracts ATTR names uppercased (=UC). Data rows use COL->AVP
 * (row-level actSpec). First cell is the REC anchor collecting ROW* (all other
 * row cells); remaining cells are plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_140/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [VAL : ROW*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
public class RtlTask140Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "140"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [ATTR=UC]+ ]
                [ COL->AVP [VAL : ROW*->REC] [VAL]+ ]+
                """;
    }
}
