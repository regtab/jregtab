package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask06: anchor cell uses CL(ST)->REC, remaining cells
 * conditionally skip blanks or extract values.
 */
class RtlTask06Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "06"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : CL(ST)->REC] [(BLANK ? SKIP | VAL)]+ ]
                  [ [(BLANK ? SKIP | VAL)]+ ]{4} }+
                """;
    }
}
