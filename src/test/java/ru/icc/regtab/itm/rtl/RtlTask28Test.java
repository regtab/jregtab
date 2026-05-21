package ru.icc.regtab.itm.rtl;

/**
 * Task 28: flat table with a header row (anchor collecting all same-subtable
 * values plus one-or-more plain VALs) and one-or-more multi-cell data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_28/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask28Test}
 * <pre>
 * [ [VAL : ST*->REC] [VAL]+ ] [ [VAL]+ ]+
 * </pre>
 * Header row: anchor VAL with ST*->REC (unbounded same-subtable collection),
 * followed by one-or-more plain VAL cells. Each data row contains one-or-more
 * plain VAL cells that feed into the anchor's REC.
 */
public class RtlTask28Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "28"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ST*->REC] [VAL]+ ] [ [VAL]+ ]+
                """;
    }
}
