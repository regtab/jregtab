package ru.icc.regtab.itm.rtl;

/**
 * Task 24: flat single-column table with a header cell collecting all values
 * below (unbounded) and one-or-more plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_24/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask24Test}
 * <pre>
 * [ [VAL : BW*->REC] ]
 * [ [VAL] ]+
 * </pre>
 * Header row: a single VAL anchor with BW*->REC (unbounded collection of all
 * cells below). Each subsequent data row contains one plain VAL that feeds
 * into the anchor's REC.
 */
public class RtlTask24Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "24"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : BW*->REC] ]
                [ [VAL] ]+
                """;
    }
}
