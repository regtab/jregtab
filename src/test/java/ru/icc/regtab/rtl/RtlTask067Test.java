package ru.icc.regtab.rtl;

/**
 * Task 67: ATTR header; repeating data rows with inherited COL-&gt;AVP — first cell is REC
 * anchor (RT*-&gt;REC) plus FILL-or-VAL conditional; remaining cells are also FILL-or-VAL
 * conditionals. Blank cells get filled from the nearest non-blank above (reverse traversal).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_067/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask067Test}
 * <pre>
 * [          [ATTR]+ ]
 * [ COL-&gt;AVP [RT*-&gt;REC (BLANK? VAL: -(AV &amp; !BLANK)-&gt;FILL | VAL)]
 *            [(BLANK? VAL: -(AV &amp; !BLANK)-&gt;FILL | VAL)]+ ]+
 * </pre>
 * COL-&gt;AVP is a row-level inherited action applied to all VAL cells. FILL uses
 * reverse-row-major traversal (-(AV &amp; !BLANK)) to propagate non-blank values downward
 * into blank cells of the same column.
 */
public class RtlTask067Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "067"; }

    @Override
    protected String buildRtl() {
        return """
                [          [ATTR]+ ]
                [ COL->AVP [RT*->REC (BLANK? VAL: -(AV & !BLANK)->FILL | VAL)] 
                           [(BLANK? VAL: -(AV & !BLANK)->FILL | VAL)]+ ]+
         """;
    }
}
