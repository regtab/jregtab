package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask09: subrow pattern with REPL extractor and conditional.
 * Row key at COL0 (RM), col key at ROW0 (CM). Blank cells are skipped.
 */
class RtlTask09Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "09"; }

    @Override
    protected String buildRtl() {
        return """
                <ANCH(2)>
                [ [] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [(BLANK? _ | VAL : (SR{1}, SC{1})->REC)]+ } ]+
                """;
    }
}
