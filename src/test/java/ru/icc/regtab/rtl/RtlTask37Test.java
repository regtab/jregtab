package ru.icc.regtab.rtl;

/**
 * Task 37: cross-table with a corner-skip header row and per-person data rows
 * using conditional blank-skipping (same structure as task 32).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_37/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask37Test}
 * <pre>
 * [ [] [VAL]+ ]
 * [ [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ ]+
 * </pre>
 * Header row: one corner skip then one-or-more column-header VALs. Data rows:
 * a plain VAL row-key anchor (person name) followed by conditional cells — blank
 * cells are skipped, non-blank cells produce REC(2) with providers SR (same subrow)
 * and SC (same subcol) for a two-axis unpivot.
 */
public class RtlTask37Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "37"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ ]+
                """;
    }
}
