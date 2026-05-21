package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 24 (Foofah exp0_33): fold a single-column list of names into one wide row (all names left-to-right).
 * <p>
 * {@code subtables().one()} keeps one subtable on the sheet. The top-left value is the {@code rec} anchor;
 * the provider takes every other value strictly below in the same column (same subtable).
 */
public final class Task24 extends TaskBase {

    private static final ItemFilterCondition BELOW_SAME_COLUMN =
            (a, c) -> c.below(a).sameCol();

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().val()
                .actions().rec(BELOW_SAME_COLUMN)
                .rows().oneOrMore()
                .cells().one().val()
                .apply(syntax);
    }
}
