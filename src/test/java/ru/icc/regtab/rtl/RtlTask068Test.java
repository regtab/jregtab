package ru.icc.regtab.rtl;

/**
 * Task 68: header rows with blank skip cell and tagged #HEAD VAL columns; data rows with
 * a non-blank VAL anchor followed by VAL cells collecting (COL &amp; #HEAD)* and same-row
 * items into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_068/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask068Test}
 * <pre>
 * [ [BLANK] [VAL #'HEAD']+ ]+
 * [ [!BLANK? VAL] [VAL: ((COL &amp; #'HEAD')*, ROW)-&gt;REC]+ ]+
 * </pre>
 * Header cells are tagged #HEAD to allow data cells to look them up by column. Each data
 * VAL in the data rows builds its record by joining same-column #HEAD header values with
 * all same-row values.
 */
public class RtlTask068Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "068"; }

    @Override
    protected String buildRtl() {
        return """
                [ [BLANK] [VAL #'HEAD']+ ]+
                [ [!BLANK? VAL] [VAL: ((COL & #'HEAD')*, ROW)->REC]+ ]+
                """;
    }
}
