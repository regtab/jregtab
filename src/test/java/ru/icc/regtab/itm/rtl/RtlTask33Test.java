package ru.icc.regtab.itm.rtl;

/**
 * Task 33: flat table where each row's anchor cell collects same-row values via
 * REC and groups rows with the same ID string via CONCAT.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask33Test}
 * <pre>
 * [ [VAL : SR*->REC, (BW & STR)*->CONCAT] [VAL]+ ]+
 * </pre>
 * Each data row: anchor VAL with SR*->REC (unbounded same-subrow collection)
 * and (BW & STR)*->CONCAT (unbounded concatenation of cells that are both below
 * and share the same string as the anchor). One-or-more plain VAL cells follow.
 */
public class RtlTask33Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "33"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : SR*->REC, (BW & STR)*->CONCAT] [VAL]+ ]+
                """;
    }
}
