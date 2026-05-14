package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask39: compound cell — price VAL (rec same-cell),
 * separator " / ", bedrooms VAL, separator "br", rest SKIP.
 */
class RtlTask39Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "39"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : CL->REC " / " VAL "br" _] ]+
                """;
    }
}
