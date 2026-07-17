package ru.icc.regtab.rtl;

/**
 * Task 32: cross-table unpivot with one skip+header row and data rows using
 * conditional blank-skipping (same structure as tasks 09/11, without subrow grouping).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_032/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask032Test}
 * <pre>
 * [ [] [VAL]+ ]
 * [ [VAL] [BLANK ? _ | VAL : (SR, SC)->REC(2)]+ ]+
 * </pre>
 * Header row: one skip cell then one-or-more column-header VALs. Data rows:
 * a plain VAL row-key anchor followed by one-or-more conditional cells — blank
 * cells are skipped, non-blank cells produce REC(2) with providers SR (same
 * subrow) and SC (same subcol) for a two-axis unpivot.
 */
public class RtlTask032Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "032"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [] [VAL]+ ] 
                [ [VAL] [BLANK ? _ | VAL : (SR, SC)->REC(2)]+ ]+
                """;
    }
}
