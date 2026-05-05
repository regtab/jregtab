package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 50 (Foofah exp0_proactive_wrangling_unfold).
 */
public final class Task50 extends TaskBase {

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions()
                .avp("")
                .rec((a,c)->c.is.in.sameRow(a))
                .concat((a,c)->c.is.below(a).sameCol() && c.has.sameStr(a))
                .cells().one().attr()
                .cells().one().val().actions().avp((a,c)->c.is.in.sameRow(a))
                .apply(syntax);
    }
}
