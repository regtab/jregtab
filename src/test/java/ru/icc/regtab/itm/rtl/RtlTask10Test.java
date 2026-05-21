package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask10: optional skip rows, data row uses CL(ROW+0)->REC,
 * optional trailing blank row.
 */
public class RtlTask10Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "10"; }

    @Override
    protected String buildRtl() {
        return """
                { [ []{4} [BLANK?] []{3} ]*
                  [ [VAL : SR*->REC] [VAL]+ ]
                  [ [BLANK?]+ ]? }+
                """;
    }
}
