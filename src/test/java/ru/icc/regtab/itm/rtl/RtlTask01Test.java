package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask01: ...
 */
public class RtlTask01Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "01"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ST*->REC] [VAL]{2} []+ ]
                [ [] [VAL]{4} []+ ] }+
                """;
    }

}
