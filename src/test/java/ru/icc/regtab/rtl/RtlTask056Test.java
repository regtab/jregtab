package ru.icc.regtab.rtl;

/**
 * Task 56: two-row-type table — first row anchors BW*-&gt;REC and gets ROW-&gt;AVP;
 * subsequent rows get only ROW-&gt;AVP. Each row is preceded by a single ATTR cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_056/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask056Test}
 * <pre>
 * [ [ATTR] [VAL : BW*-&gt;REC, ROW-&gt;AVP]+ ]
 * [ [ATTR] [VAL : ROW-&gt;AVP]+ ]+
 * </pre>
 * The first row: each VAL cell is a record anchor collecting all below items (BW*) and
 * gets its attribute-value pair from the same-row ATTR (ROW-&gt;AVP). Repeating subsequent
 * rows: VAL cells inherit only the ROW-&gt;AVP attribute lookup.
 */
public class RtlTask056Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "056"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR] [VAL : BW*->REC, ROW->AVP]+ ]
                [ [ATTR] [VAL : ROW->AVP]+ ]+
                """;
    }
}
