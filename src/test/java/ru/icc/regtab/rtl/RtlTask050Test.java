package ru.icc.regtab.rtl;

/**
 * Task 50: single (non-repeating) flat table with non-blank three-cell rows —
 * anchor VAL (AVP + same-row REC + below-same-string JOIN(0)), ATTR, and AVP VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_050/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask050Test}
 * <pre>
 * [ [!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+
 * </pre>
 * Same pattern as task 46 but without the outer subtable repetition ({...}+).
 * Each non-blank row: anchor VAL with empty-literal AVP, SR*->REC (unbounded
 * same-subrow collection), and BW&STR*->JOIN(0) (grouping rows with the
 * same string below); non-blank ATTR cell; non-blank VAL with SR->AVP (same-subrow
 * attribute lookup).
 */
public class RtlTask050Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "050"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+
                """;
    }
}
