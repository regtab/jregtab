package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask17: header cell uses DW->REC for all non-blank cells below.
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
