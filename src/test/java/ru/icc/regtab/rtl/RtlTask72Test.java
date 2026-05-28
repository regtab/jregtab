package ru.icc.regtab.rtl;

/**
 * Task 72: ATTR header; repeating data rows with inherited COL-&gt;AVP and conditional cells —
 * blank cells are skipped (_), the first non-blank cell is an anchor (RT*-&gt;REC), remaining
 * non-blank cells are plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_72/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask72Test}
 * <pre>
 * [          [ATTR]+ ]
 * [ COL-&gt;AVP [(BLANK? _ | VAL: RT*-&gt;REC)] [(BLANK? _ | VAL)]+ ]+
 * </pre>
 * The _ (skip) branch in the conditional discards blank cells without creating any item.
 * This differs from a plain BLANK? cell which would create a blank item; here blank cells
 * are entirely transparent to the record structure.
 */
public class RtlTask72Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "72"; }

    @Override
    protected String buildRtl() {
        return """
                [          [ATTR]+ ]
                [ COL->AVP [(BLANK? _ | VAL: RT*->REC)] [(BLANK? _ | VAL)]+ ]+
                """;
    }
}
