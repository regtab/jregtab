package ru.icc.regtab.itm.rtl;

/**
 * Task 20: flat table with a two-cell header row (anchor collecting all
 * same-subtable values) and one-or-more two-cell data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_20/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask20Test}
 * <pre>
 * [ [VAL : ST*->REC] [VAL] ] [ [VAL]{2} ]+
 * </pre>
 * Header row: first cell is a VAL anchor with ST*->REC (unbounded collection
 * of all values in the same subtable); second cell is a plain VAL. Each data
 * row contains exactly two plain VAL cells that feed into the anchor's REC.
 */
public class RtlTask20Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "20"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ST*->REC] [VAL] ] [ [VAL]{2} ]+
                """;
    }
}
