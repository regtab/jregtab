package ru.icc.regtab.rtl;

/**
 * Task 107: cross-tabulation with multi-level column headers (H) and row headers (S).
 * Header rows use FILL to propagate non-blank values right into blank cells.
 * Data cells are numeric; each creates a record via (COL&#'H'*,ROW&#'S')->REC,
 * gathering all H items from the same column and S items from the same row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_107/}
 * <pre>
 * $V=['\\d+' ? VAL: (COL&amp;#'H'*,ROW&amp;#'S'*)-&gt;REC]
 * [ [BLANK]+ [!BLANK ? VAL#'H'] [BLANK ? VAL#'H': -LT&amp;!BLANK-&gt;FILL | VAL#'H']+ ]+
 * {
 * [ ['\\D.*' ? VAL#'S']+ [$V]+ ]
 * [ [BLANK ? VAL#'S': SC-&gt;FILL]+ ['\\D.*' ? VAL#'S']+ [$V]+ ]*
 * }+
 * </pre>
 */
public class RtlTask107Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "107"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                $V=['\\d+' ? VAL: (COL&#'H'*,ROW&#'S'*)->REC]
                [ [BLANK]+ [!BLANK ? VAL#'H'] [BLANK ? VAL#'H': -LT&!BLANK->FILL | VAL#'H']+ ]+
                {
                [ ['\\D.*' ? VAL#'S']+ [$V]+ ]
                [ [BLANK ? VAL#'S': SC->FILL]+ ['\\D.*' ? VAL#'S']+ [$V]+ ]*
                }+
                """;
    }
}
