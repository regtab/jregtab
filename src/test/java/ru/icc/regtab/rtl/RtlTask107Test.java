package ru.icc.regtab.rtl;

/**
 * Task 107: cross-tabulation with multi-level column headers (H) and row headers (S).
 * Header rows use FILL to propagate non-blank values right into blank cells.
 * Data cells are numeric; each creates a record via ((COL & #'H')*,(ROW & #'S'))->REC,
 * gathering all H items from the same column and S items from the same row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_107/}
 * <pre>
 * [ [BLANK?]+ [!BLANK ? VAL#'H'] [(BLANK ? VAL#'H': -(LT & !BLANK)->FILL | VAL#'H')]+ ]+
 * {
 * [ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL & #'H')*,(ROW & #'S')*)->REC]+ ]
 * [ [BLANK ? VAL#'S': SC->FILL]+ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL & #'H')*,(ROW & #'S')*)->REC]+ ]*
 * }+
 * </pre>
 */
public class RtlTask107Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "107"; }

    @Override
    protected String buildRtl() {
        return """
                [ [BLANK?]+ [!BLANK ? VAL#'H'] [(BLANK ? VAL#'H': -(LT & !BLANK)->FILL | VAL#'H')]+ ]+
                {
                [ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL & #'H')*,(ROW & #'S')*)->REC]+ ]
                [ [BLANK ? VAL#'S': SC->FILL]+ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL & #'H')*,(ROW & #'S')*)->REC]+ ]*
                }+
                """;
    }
}
