package ru.icc.regtab.itm.rtl;

/**
 * Task 19: repeated subtables with a single-cell header that collects all
 * values below (unbounded), followed by exactly 3 plain value data rows.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask19Test}
 * <pre>
 * { [ [VAL : BW*->REC] ] [ [VAL] ]{3} }+
 * </pre>
 * Each subtable has one header row with a VAL anchor using BW*->REC (unbounded
 * collection of all cells below). Then exactly 3 single-cell data rows each
 * with a plain VAL, all feeding into the header's REC.
 */
public class RtlTask19Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "19"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] [ [VAL] ]{3} }+
                """;
    }
}
