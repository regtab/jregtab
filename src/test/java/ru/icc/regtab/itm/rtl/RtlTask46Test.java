package ru.icc.regtab.itm.rtl;

/**
 * Task 46: repeated subtables with one-or-more non-blank three-cell rows —
 * anchor VAL (AVP + same-row REC + below-same-string CONCAT), ATTR, and AVP VAL.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask46Test}
 * <pre>
 * { [ [!BLANK? VAL : ''->AVP, SR*->REC, (BW & STR)*->CONCAT] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
 * </pre>
 * Each row of each subtable: non-blank anchor VAL with empty-literal AVP,
 * SR*->REC (unbounded same-subrow collection), and (BW & STR)*->CONCAT
 * (grouping rows with the same string below); non-blank ATTR cell; non-blank
 * VAL with SR->AVP (same-subrow attribute lookup).
 */
public class RtlTask46Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "46"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : ''->AVP, SR*->REC, (BW & STR)*->CONCAT] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
                """;
    }
}
