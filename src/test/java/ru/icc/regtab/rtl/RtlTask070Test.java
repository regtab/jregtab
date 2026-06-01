package ru.icc.regtab.rtl;

/**
 * Task 70: header rows with blank skip cells and tagged #H VAL columns; data rows with
 * non-digit #S VAL cells and digit VAL cells that collect (COL &amp; #H)* + (ROW &amp; #S)*
 * into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_070/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask070Test}
 * <pre>
 * [ [BLANK?]+           [VAL#'H']+ ]+
 * [ [!'\\d+'? VAL#'S']+ ['\\d+'? VAL: ((COL &amp; #'H')*, (ROW &amp; #'S')*)-&gt;REC]+ ]+
 * </pre>
 * The !\d+ cell condition distinguishes label (#S) columns from value columns. Digit-valued
 * cells build their record by merging column headers (same-column #H items) with row labels
 * (same-row #S items).
 */
public class RtlTask070Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "070"; }

    @Override
    protected String buildRtl() {
        return """
                [ [BLANK?]+           [VAL#'H']+ ]+
                [ [!'\\d+'? VAL#'S']+ ['\\d+'? VAL: ((COL & #'H')*, (ROW & #'S')*)->REC]+ ]+
                """;
    }
}
