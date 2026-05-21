package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 48 (Foofah exp0_proactive_wrangling_complex).
 */
public final class Task48 extends TaskBase {

    private static final ItemFilterCondition SAME_SUBTABLE_COL1 =
            (a, c) -> c.sameSubtable(a) && c.col(1);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one() // Первые две строки составляют первую подтаблицу.
                .rows().exactly(2)
                .cells().exactly(2).skip()
                .rows().oneOrMore()
                .cells().one()
                .when(Cell::textBlank).skip()
                .otherwise().val()
                .actions()
                .avp("")
                .rec(SAME_SUBTABLE_COL1)
                .cells().one()
                .when(Cell::textBlank).skip()
                .otherwise()
                .attr()
                .sep(":")
                .val()
                .actions().avp((a, c) -> c.sameCell(a))
                .apply(syntax);
    }
}
