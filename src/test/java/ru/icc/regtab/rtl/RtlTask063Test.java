package ru.icc.regtab.rtl;

/**
 * Task 63: repeating data rows with reverse-traversal COL-&gt;AVP (row-level), anchor VAL
 * (RT*-&gt;REC) and zero-or-more plain VAL cells; terminated by a single ATTR footer row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_063/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask063Test}
 * <pre>
 * [ -COL-&gt;AVP [VAL: RT*-&gt;REC] [VAL]* ]+
 * [           [ATTR]+ ]
 * </pre>
 * The -COL-&gt;AVP row-level action uses reverse-row-major traversal to look up column
 * attributes below the anchor. The data rows are followed by a footer row of ATTR cells
 * that supply the column-attribute values.
 */
public class RtlTask063Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "063"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ -COL->AVP [VAL: RT*->REC] [VAL]* ]+
                [           [ATTR]+ ]
                """;
    }
}
