package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 49 (Foofah exp0_proactive_wrangling_fold).
 */
public final class Task49 extends TaskBase {

    private static final ProviderSpec FIRST_SAME_ROW =
            ProviderSpec.val((a,c)->c.sameRow(a), 1);
    private static final ProviderSpec FIRST_SAME_COL =
            ProviderSpec.val((a,c)->c.sameCol(a), 1);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().skip()
                .cells().oneOrMore().val()
                .rows().oneOrMore()
                .cells().one().val()
                .cells().oneOrMore().val().actions().rec(FIRST_SAME_ROW, FIRST_SAME_COL)
                .apply(syntax);
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
