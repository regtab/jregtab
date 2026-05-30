package ru.icc.regtab.rtl;

/**
 * Task 78: single non-repeating subtable with ROW-&gt;AVP — first row has ATTR + VAL anchors
 * (BW*-&gt;REC); subsequent rows have ATTR + plain VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_78/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask78Test}
 * <pre>
 * { ROW-&gt;AVP
 * [ [ATTR] [VAL: BW*-&gt;REC]+ ]
 * [ [ATTR] [VAL]+ ]+ }
 * </pre>
 * The subtable-level ROW-&gt;AVP propagates row-ATTR lookup to all VAL cells. The first row's
 * VAL cells additionally anchor records by collecting all below items (BW*). Subsequent
 * rows contribute their values into those records.
 */
public class RtlTask78Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "78"; }

    @Override
    protected String buildRtl() {
        return """
                ROW->AVP
                [ [ATTR] [VAL: BW*->REC]+ ]
                [ [ATTR] [VAL]+ ]+
                """;
    }
}
