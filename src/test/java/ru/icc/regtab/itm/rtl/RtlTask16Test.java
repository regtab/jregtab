package ru.icc.regtab.itm.rtl;

/**
 * Task 16: flat table where each anchor cell in column 0 collects one value
 * to the right via REC and concatenates same-string cells below via CONCAT.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask16Test}
 * <pre>
 * [ [VAL : RT->REC, (BW & STR)*->CONCAT] [VAL] ]+
 * </pre>
 * Data rows: anchor VAL uses RT->REC (1 value immediately to the right) and
 * (BW & STR)*->CONCAT (unbounded concatenation of cells that are both below
 * and have the same string as the anchor), followed by a plain VAL cell.
 */
public class RtlTask16Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "16"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT->REC, (BW & STR)*->CONCAT] [VAL] ]+
                """;
    }
}
