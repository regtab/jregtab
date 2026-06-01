package ru.icc.regtab.rtl;

/**
 * Task 77: exactly 2 anchor VAL rows — each cell collects the cell 2 rows below (BW &amp; R+2)
 * into REC; followed by exactly 2 plain VAL rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_077/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask077Test}
 * <pre>
 * [ [VAL: (BW &amp; R+2)-&gt;REC]+ ]{2}
 * [ [VAL]+ ]{2}
 * </pre>
 * The R+2 row-offset constraint restricts the below provider to items exactly 2 rows below
 * the anchor. The {2} quantifiers enforce that both the anchor block and the data block each
 * contain exactly 2 rows.
 */
public class RtlTask077Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "077"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: (BW & R+2)->REC]+ ]{2}
                [ [VAL]+ ]{2}
                """;
    }
}
