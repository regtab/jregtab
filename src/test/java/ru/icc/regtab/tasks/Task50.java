package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

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
                .rec((a,c)->c.sameRow(a))
                .concat((a,c)->c.below(a).sameCol() && c.sameStr(a))
                .cells().one().attr()
                .cells().one().val().actions().avp((a,c)->c.sameRow(a))
                .apply(syntax);
    }
}
