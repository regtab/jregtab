package ru.icc.regtab.rtl;

/**
 * Task 109: each row has two label anchors followed by ODD/EVEN data pairs.
 * Anchor 1 collects all same-row #ODD items; anchor 2 collects all #EVEN items.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_109/}
 * <pre>
 * [ [VAL: ROW&amp;#'ODD'*-&gt;REC] [VAL: ROW&amp;#'EVEN'*-&gt;REC] { [VAL#'ODD'] [VAL#'EVEN'] }+ ]+
 * </pre>
 * Data cells alternate #ODD / #EVEN. Each anchor gathers its tagged peers via
 * ROW (same row, not same cell) with unbounded cardinality.
 */
public class RtlTask109Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "109"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: ROW&#'ODD'*->REC] [VAL: ROW&#'EVEN'*->REC] { [VAL#'ODD'] [VAL#'EVEN'] }+ ]+
                """;
    }
}
