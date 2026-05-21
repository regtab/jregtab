package ru.icc.regtab.itm.rtl;

/**
 * Task 22: repeated subtables where the anchor collects values in columns 2–5
 * in column-major traversal order, plus a plain data row.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask22Test}
 * <pre>
 * { [ [VAL : ^(ST & C2..5)*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
 * </pre>
 * Header row: anchor VAL with column-major REC over same-subtable columns 2–5
 * (^(ST & C2..5)*), one skip, then one-or-more plain VALs. Data row: two
 * skipped cells then one-or-more plain VALs contributing to the REC.
 */
public class RtlTask22Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "22"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^(ST & C2..5)*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
                """;
    }
}
