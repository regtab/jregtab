package ru.icc.regtab.itm.rtl;

/**
 * Task 05: cross-table unpivot — one column-header row, one skip row, then
 * data rows with a row-key anchor and value cells referencing both axes.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_05/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask05Test}
 * <pre>
 * [ [] [VAL]+ ]
 * [ []+ ]
 * [ [VAL] [VAL : (SR, SC)->REC(2)]+ ]+
 * </pre>
 * First row: skip cell then one-or-more column-header VALs. Second row: all
 * skipped. Data rows: plain VAL row-key anchor then value cells each producing
 * REC(2) with providers SR (same subrow, row-key) and SC (same subcol,
 * col-key), performing a full two-axis unpivot.
 */
public class RtlTask05Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "05"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ []+ ]
                [ [VAL] [VAL : (SR, SC)->REC(2)]+ ]+
                """;
    }
}
