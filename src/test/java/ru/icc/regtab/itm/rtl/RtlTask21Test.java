package ru.icc.regtab.itm.rtl;

/**
 * Task 21: repeated subtables where a multi-cell normalised header row collects
 * all values below (unbounded), followed by exactly 2 normalised data rows.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask21Test}
 * <pre>
 * { [ [VAL=NORM : BW*->REC]+ ] [ [VAL=NORM]+ ]{2} }+
 * </pre>
 * Header row: one-or-more whitespace-normalised VAL cells each with BW*->REC
 * (unbounded collection of all cells below in the same column). Exactly 2 data
 * rows follow, each with one-or-more normalised VAL cells.
 */
public class RtlTask21Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "21"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL=NORM : BW*->REC]+ ] [ [VAL=NORM]+ ]{2} }+
                """;
    }
}
