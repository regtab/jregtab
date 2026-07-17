package ru.icc.regtab.rtl;

/**
 * Task 09: cross-table unpivot with regex-cleaned column headers, explicit
 * subrow grouping, and conditional blank-skipping in data cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_009/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask009Test}
 * <pre>
 * [ [] [VAL = REPL('\s+', '')]{5} ]
 * [ { [VAL] [BLANK? _ | VAL : (SR, SC)->REC(2)]+ } ]+
 * </pre>
 * Header row: skip cell then exactly 5 column-header VALs with whitespace
 * stripped via REPL. Data rows use an explicit subrow block: a row-key VAL
 * anchor followed by one-or-more conditional cells — blank cells are skipped,
 * non-blank cells produce REC(2) with providers SR (same subrow) and SC
 * (same subcol) for a two-axis unpivot.
 */
public class RtlTask009Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "009"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [BLANK? _ | VAL : (SR, SC)->REC(2)]+ } ]+
                """;
    }
}
