package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask14: header row with blank sentinel, data rows look up
 * first items in C0 and C1 of same subtable, plus two items from same row.
 */
class RtlTask14Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "14"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK ? VAL]{2} [BLANK?] ]
                  [ [!BLANK ? VAL]{2} [!BLANK ? VAL : ((ST, C0){1}, (ST, C1){1}, SR{2})->REC(4)] ]+ }+
                """;
    }
}
