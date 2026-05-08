package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 19 (Foofah exp0_26): one column of fields; every four rows (name, age, gender, birthday) form a subtable.
 * The first row’s cell is a {@code rec} anchor; {@code rec} collects the three value cells below (same column, same subtable).
 */
public final class Task19 extends TaskBase {

    private static final ItemFilterCondition REC_BELOW_SAME_COL =
            (a, c) -> c.below(a).sameCol() && c.sameSubtable(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(REC_BELOW_SAME_COL)
                .rows().exactly(3)
                .cells().one().val()
                .apply(syntax);
    }
}
