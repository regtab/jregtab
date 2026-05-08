package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 12 (Foofah exp0_15): stacked ledger lines (fund id, line no., amount in column 5) folded into one wide row —
 * fund id from the first cell of the first line; each line’s amount is in column 5 (0-based). {@code rec} on that
 * first cell collects every value item in column 5 (row-major), producing one record with the id plus all amounts.
 */
public final class Task12 extends TaskBase {

    private static final ItemFilterCondition AMOUNT_COLUMN = (a, c) -> c.col(5);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().val()
                .actions().rec(AMOUNT_COLUMN)
                .cells().exactly(4).skip()
                .cells().one().val()
                .rows().oneOrMore()
                .cells().exactly(5).skip()
                .cells().one().val()
                .apply(syntax);
    }
}
