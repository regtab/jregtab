package ru.icc.regtab.rtl;

/**
 * Task 64: two-row-type table — first row anchors BW*-&gt;REC and gets reverse ROW-&gt;AVP
 * (-ROW-&gt;AVP); subsequent rows get only -ROW-&gt;AVP. Each row ends with a single ATTR cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_64/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask64Test}
 * <pre>
 * [ [VAL: BW*-&gt;REC, -ROW-&gt;AVP]+ [ATTR] ]
 * [ [VAL: -ROW-&gt;AVP]+            [ATTR] ]+
 * </pre>
 * -ROW-&gt;AVP uses reverse-row-major traversal to find the row's ATTR cell. The first row
 * additionally anchors a record collecting all below items (BW*). Subsequent rows carry
 * only the attribute lookup.
 */
public class RtlTask64Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "64"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: BW*->REC, -ROW->AVP]+ [ATTR] ]
                [ [VAL: -ROW->AVP]+           [ATTR] ]+
                """;
    }
}
