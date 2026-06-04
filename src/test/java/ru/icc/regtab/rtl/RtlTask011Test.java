package ru.icc.regtab.rtl;

/**
 * Task 11: cross-table unpivot with a column-header row and data rows using
 * explicit subrow grouping and conditional blank-skipping (same structure as task 09,
 * without the regex-cleaned headers).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_011/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask011Test}
 * <pre>
 * [ [] [VAL]+ ]
 * [ { [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ } ]+
 * </pre>
 * First row: skip cell then one-or-more plain column-header VALs. Data rows use
 * an explicit subrow: a row-key VAL anchor followed by conditional cells — blank
 * cells are skipped, non-blank cells produce REC(2) with providers SR (same
 * subrow) and SC (same subcol) for a two-axis unpivot.
 */
public class RtlTask011Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "011"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ { [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ } ]+
                """;
    }
}
