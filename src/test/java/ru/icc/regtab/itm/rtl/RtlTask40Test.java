package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask40: repeating crime-report blocks.
 * Title row: VAL matching "Reported crime in", stripped prefix, avp(""), rec(col1 in subtable).
 * Header skip row (2 cells), 5 attr/val rows, optional trailing skip row.
 */
class RtlTask40Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "40"; }

    @Override
    protected String buildRtl() {
        return """
                { [ ['.*Reported crime in.*'? VAL = REPL('Reported crime in ', '') : ''->AVP, CL(ST, C1)->REC] [SKIP] ]
                  [ [SKIP]{2} ]
                  [ [ATTR] [VAL : RM{1}->AVP] ]{5}
                  [ [SKIP]{2} ]? }+
                """;
    }
}
