package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 33 (Foofah exp0_46): fold rows by first column — within each group, concatenate all cell values in row order
 * into one wide record. First column cell is the {@code rec} anchor; {@code O_rec} collects same-row value items;
 * {@code O_concat} merges sequences from subsequent rows whose first cell matches the anchor (same group id).
 */
public final class Task33 extends TaskBase {

    private static final ItemFilterCondition SAME_ROW = (a, c) -> c.sameRow(a);

    private static final ItemFilterCondition SAME_GROUP_NEXT_ROWS =
            (a, c) -> c.below(a).sameCol() && c.sameStr(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().rec(SAME_ROW)
                .concat(SAME_GROUP_NEXT_ROWS)
                .cells().oneOrMore().val()
                .apply(syntax);
    }
}
