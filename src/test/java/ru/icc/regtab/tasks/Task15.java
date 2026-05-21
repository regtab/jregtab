package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 15: single column of space-separated quadruples (time + three measurements);
 * unpivot to (time, value) rows. Each measurement token is a rec anchor; the provider picks the first
 * other item in the same cell (the time token at index 0). Three anchors per row are required so the
 * engine emits one record per measurement (one anchor with three identical providers would repeat the same candidate).
 */
public final class Task15 extends TaskBase {

    private static final ProviderSpec FIRST_IN_SAME_CELL =
            ProviderSpec.any((a, c) -> c.sameCell(a), 1);

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().compound()
                .val()
                .sep(" ")
                .val().actions().rec(FIRST_IN_SAME_CELL)
                .sep(" ")
                .val().actions().rec(FIRST_IN_SAME_CELL)
                .sep(" ")
                .val().actions().rec(FIRST_IN_SAME_CELL)
                .apply(syntax);
    }
}
