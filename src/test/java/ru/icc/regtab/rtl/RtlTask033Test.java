package ru.icc.regtab.rtl;

/**
 * Task 33: flat table where each row's anchor cell collects same-row values via
 * REC and groups rows with the same ID string via JOIN(0).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_033/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask033Test}
 * <pre>
 * [ [VAL : SR*->REC, (BW & STR)*->JOIN(0)] [VAL]+ ]+
 * </pre>
 * Each data row: anchor VAL with SR*->REC (unbounded same-subrow collection)
 * and (BW & STR)*->JOIN(0) (unbounded concatenation of cells that are both below
 * and share the same string as the anchor). One-or-more plain VAL cells follow.
 */
public class RtlTask033Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "033"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : SR*->REC, (BW & STR)*->JOIN(0)] [VAL]+ ]+
                """;
    }
}
