package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask02: two header rows (#L1, #L2), data rows look up
 * both headers via CL(ST, TAG #L1 #L2) and same-row anchor via CL{1}(ROW+0).
 */
class RtlTask02Test extends RtlTaskBase {

    @Override
    protected String taskId() {
        return "02";
    }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR{1})->REC(2)] [VAL] ]+
                  [ [BLANK?] [] ]? }+
                """;
    }
}
