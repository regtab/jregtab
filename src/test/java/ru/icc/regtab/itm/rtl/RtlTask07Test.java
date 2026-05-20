package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask07: three row keys at C0..2, col key at R0.
 */
public class RtlTask07Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "07"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{3} [VAL]+ ]
                [ [VAL]{3} [VAL : ((SR & C0..2){3}, SC)->REC(4)]+ ]+
                """;
    }
}
