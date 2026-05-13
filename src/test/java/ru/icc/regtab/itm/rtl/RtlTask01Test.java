package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask01: ...
 */
class RtlTask01Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "01"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : CL(ST)->REC] [VAL]{2} [SKIP]+ ]
                [ [SKIP] [VAL]{4} [SKIP]+ ] }+
                """;
    }

}
