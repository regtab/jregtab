package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 46 (Foofah exp0_potters_wheel_unfold): длинный формат (имя, предмет, балл) → широкая таблица.
 * Черновик — см. {@link #buildItm(TableSyntax)}.
 */
public final class Task46 extends TaskBase {

    private static final ItemFilterCondition SAME_ROW =
            (a, c) -> c.sameRow(a);

    private static final ItemFilterCondition SAME_NAME_BELOW =
            (a, c) -> c.below(a).sameCol() && c.sameStr(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().avp("").rec(SAME_ROW).concat(SAME_NAME_BELOW)
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(SAME_ROW)
                .apply(syntax);
    }
}
