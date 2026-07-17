package ru.icc.regtab.rtl;

/**
 * Task 58: repeating rows of one-or-more compound cells, each containing a trimmed
 * VAL anchor (CL-&gt;REC, same-cell) followed by '=' and a second trimmed VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_058/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask058Test}
 * <pre>
 * [ [VAL=TRIM: CL-&gt;REC '=' VAL=TRIM]+ ]+
 * </pre>
 * Every cell is a key=value compound: the left segment is the trimmed anchor VAL that
 * links to its own same-cell item (CL-&gt;REC); the right segment after '=' is the trimmed
 * value VAL.
 */
public class RtlTask058Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "058"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
               [ [VAL=TRIM: CL->REC '=' VAL=TRIM]+ ]+
                """;
    }
}
