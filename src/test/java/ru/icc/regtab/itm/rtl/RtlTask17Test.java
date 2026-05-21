package ru.icc.regtab.itm.rtl;

/**
 * Task 17: repeated subtables where the header cell collects all values below
 * (unbounded), followed by one-or-more non-blank data rows and an optional skip row.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask17Test}
 * <pre>
 * { [ [VAL : BW*->REC] ] [ [!BLANK? VAL] ]+ [ [] ]? }+
 * </pre>
 * Each subtable has a single-cell header row whose VAL anchors an unbounded
 * REC collecting all cells below (BW*). Then one-or-more rows each with a
 * non-blank VAL, followed by an optional empty row separator.
 */
public class RtlTask17Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "17"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] [ [!BLANK? VAL] ]+ [ [] ]? }+
                """;
    }
}
