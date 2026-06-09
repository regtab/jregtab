package ru.icc.regtab.rtl;

/**
 * Task 142: SEZ TRT residents table with fill on the first column.
 * Header row extracts ATTR names uppercased (=UC). Data rows use COL->AVP
 * (row-level actSpec). First column (SEZ TRT) may be blank: blank cells inherit
 * the nearest non-blank value above via -AV&amp;!BLANK->FILL and create REC;
 * non-blank cells create REC directly. Both branches collect ROW* (all other
 * row cells).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_142/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, ROW*-&gt;REC | VAL : ROW*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
public class RtlTask142Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "142"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR=UC]+ ]
                [ COL->AVP [BLANK ? VAL : -AV&!BLANK->FILL, ROW*->REC | VAL : ROW*->REC] [VAL]+ ]+
                """;
    }
}
