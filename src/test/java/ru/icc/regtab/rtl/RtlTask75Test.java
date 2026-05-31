package ru.icc.regtab.rtl;

/**
 * Task 75: repeating rows — anchor VAL (RT*-&gt;REC) and conditional cells: blank cells are
 * filled from the nearest non-blank left neighbour (reverse-row-major traversal), non-blank
 * cells remain plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_75/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask75Test}
 * <pre>
 * [ [VAL: RT*-&gt;REC] [(BLANK? VAL: -(LT &amp; !BLANK)-&gt;FILL | VAL)]+ ]+
 * </pre>
 * FILL uses -(LT &amp; !BLANK) — reverse-row-major traversal restricted to non-blank left-of
 * cells — to propagate values rightward into blank cells within the same row.
 */
public class RtlTask75Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "75"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: RT*->REC] [(BLANK? VAL: -(LT & !BLANK)->FILL | VAL)]+ ]+
                """;
    }
}
