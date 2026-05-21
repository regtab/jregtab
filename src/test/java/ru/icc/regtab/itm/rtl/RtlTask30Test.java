package ru.icc.regtab.itm.rtl;

/**
 * Task 30: repeated subtables with a single-cell header collecting all values
 * below (unbounded), followed by exactly 3 plain data rows.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask30Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [VAL] ]{3} }+
 * </pre>
 * Structurally identical to task 19: each subtable has a header row with a VAL
 * anchor using BW*->REC (unbounded collection of cells below), then exactly 3
 * single-cell data rows each contributing a plain VAL.
 */
public class RtlTask30Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "30"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [VAL] ]{3} }+
                """;
    }
}
