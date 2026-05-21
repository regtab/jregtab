package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask15: compound spec with three CL->REC segments.
 */
public class RtlTask15Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "15"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1)] ]+
                """;
    }
}
