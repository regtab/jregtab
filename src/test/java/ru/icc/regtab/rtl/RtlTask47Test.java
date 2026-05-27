package ru.icc.regtab.rtl;

/**
 * Task 47: repeated subtables with one-or-more non-blank two-cell rows —
 * anchor VAL with same-row REC and below-same-string JOIN(0), plus a plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_47/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask47Test}
 * <pre>
 * { [ [!BLANK? VAL : SR*->REC, (BW & STR)*->JOIN(0)] [!BLANK? VAL] ]+ }+
 * </pre>
 * Each row of each subtable: non-blank anchor VAL with SR*->REC (unbounded
 * same-subrow collection) and (BW & STR)*->JOIN(0) (grouping rows with the
 * same string below); followed by a non-blank plain VAL cell.
 */
public class RtlTask47Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "47"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : SR*->REC, (BW & STR)*->JOIN(0)] [!BLANK? VAL] ]+ }+
                """;
    }
}
