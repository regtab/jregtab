package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 47 (Foofah exp0_potters_wheel_unfold2): фамилия + имя в двух колонках → широкая строка на фамилию
 * (несколько имён в соседних колонках). Черновик — см. {@link #buildItm(TableSyntax)}.
 */
public final class Task47 extends TaskBase {

    private static final ItemFilterCondition SAME_ROW =
            (a, c) -> c.is.in.sameRow(a);

    private static final ItemFilterCondition SAME_SURNAME_BELOW =
            (a, c) -> c.is.below(a).sameCol() && c.has.sameStr(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().rec(SAME_ROW).concat(SAME_SURNAME_BELOW)
                .cells().one().val()
                .apply(syntax);
    }
}
