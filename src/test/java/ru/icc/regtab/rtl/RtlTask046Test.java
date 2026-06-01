package ru.icc.regtab.rtl;

/**
 * Task 46: repeated subtables with one-or-more non-blank three-cell rows —
 * anchor VAL (AVP + same-row REC + below-same-string JOIN(0)), ATTR, and AVP VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_046/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask046Test}
 * <pre>
 * { [ [!BLANK? VAL : ''->AVP, SR*->REC, (BW & STR)*->JOIN(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
 * </pre>
 * Each row of each subtable: non-blank anchor VAL with empty-literal AVP,
 * SR*->REC (unbounded same-subrow collection), and (BW & STR)*->JOIN(0)
 * (grouping rows with the same string below); non-blank ATTR cell; non-blank
 * VAL with SR->AVP (same-subrow attribute lookup).
 */
public class RtlTask046Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "046"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : ''->AVP, SR*->REC, (BW & STR)*->JOIN(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
                """;
    }
}
