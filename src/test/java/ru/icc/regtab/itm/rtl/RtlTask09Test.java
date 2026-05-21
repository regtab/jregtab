package ru.icc.regtab.itm.rtl;

/**
 * Task 09: cross-table unpivot with regex-cleaned column headers, explicit
 * subrow grouping, and conditional blank-skipping in data cells.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask09Test}
 * <pre>
 * [ [] [VAL = REPL('\s+', '')]{5} ]
 * [ { [VAL] [(BLANK? _ | VAL : (SR, SC)->REC(2))]+ } ]+
 * </pre>
 * Header row: skip cell then exactly 5 column-header VALs with whitespace
 * stripped via REPL. Data rows use an explicit subrow block: a row-key VAL
 * anchor followed by one-or-more conditional cells — blank cells are skipped,
 * non-blank cells produce REC(2) with providers SR (same subrow) and SC
 * (same subcol) for a two-axis unpivot.
 */
public class RtlTask09Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "09"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [(BLANK? _ | VAL : (SR, SC)->REC(2))]+ } ]+
                """;
    }
}
