package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask41: alternating two-column rows (compound fill+rec) and
 * single-column rows (val rec right+ctxAux). Two optional rows per subtable iteration.
 */
class RtlTask41Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "41"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : ('')->FILL, (CL, RW{1})->REC "" VAL] [!BLANK? VAL] ]?
                  [ [!BLANK? VAL : (RW{1}, '')->REC] [BLANK? SKIP] ]? }+
                """;
    }
}
