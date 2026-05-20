package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask35: one-or-more subtables each with a header row
 * matching "*Company" (VAL = REPL('\*','') : DW(ST)->REC) and one-or-more
 * data rows whose cell does NOT match "*Company".
 */
class RtlTask35Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "35"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [~'*Company' ? VAL = REPL('\\*', '') : ^ST*->REC] ]
                  [ [!~'*Company' ? VAL] ]+ }+
                """;
    }
}
