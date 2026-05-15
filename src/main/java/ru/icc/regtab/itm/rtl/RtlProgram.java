package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.RecordsetTransformation;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.List;

public record RtlProgram(TablePattern tablePattern, List<RecordsetTransformation> transformations) {

    public Recordset transform(Recordset rs) {
        var result = rs;
        for (var t : transformations) result = t.apply(result);
        return result;
    }
}
