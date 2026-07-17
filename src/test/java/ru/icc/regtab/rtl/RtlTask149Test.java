package ru.icc.regtab.rtl;

/**
 * Task 149: tourist-recreational cluster objects table — flat layout with fill.
 * Header row extracts ATTR names uppercased (=UC). Data rows use COL->AVP
 * (row-level actSpec). First column (cluster) may be blank: blank cells use
 * -AV&amp;!BLANK->FILL and create REC via ROW*; non-blank cells create REC via
 * ROW* directly. Remaining columns are plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_149/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP
 *   [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, ROW*-&gt;REC | VAL : ROW*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
public class RtlTask149Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "149"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [ATTR=UC]+ ]
                [ COL->AVP
                  [BLANK ? VAL : -AV&!BLANK->FILL, ROW*->REC | VAL : ROW*->REC] [VAL]+ ]+
                """;
    }
}
