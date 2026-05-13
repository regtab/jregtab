package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask15: compound spec with three CL{1}->REC segments.
 */
class RtlTask15Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "15"; }

    @Override
    protected String buildRtl() {
        return "[ [VAL ' ' VAL : (CL{1})->REC ' ' VAL : (CL{1})->REC ' ' VAL : (CL{1})->REC] ]+";
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }
}
