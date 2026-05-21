package ru.icc.regtab.itm.rtl;

/**
 * Task 27: repeated subtables with a single-cell header collecting all values
 * below, one skip row separator, and exactly 9 plain data rows.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask27Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [] ]
 *   [ [VAL] ]{9} }+
 * </pre>
 * Header row: a single VAL anchor with BW*->REC (unbounded collection of cells
 * below). A one-cell skip row acts as a separator. Then exactly 9 single-cell
 * data rows each contributing a plain VAL to the REC.
 */
public class RtlTask27Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "27"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [] ] 
                  [ [VAL] ]{9} }+
                """;
    }
}
