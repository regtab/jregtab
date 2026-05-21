package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 20 (Foofah exp0_27): fold a 2-column table into one wide row — all cell values in row-major order.
 * <p>
 * {@code subtables().one()} keeps the whole sheet as one subtable (no heuristic boundaries). Only the
 * top-left value item has {@code rec}; {@code sameSubtable} collects the rest in row-major order.
 */
public final class Task20 extends TaskBase {

    private static final ItemFilterCondition REC_REST_OF_SUBTABLE =
            (a, c) -> c.sameSubtable(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().val()
                .actions().rec(REC_REST_OF_SUBTABLE)
                .cells().one().val()
                .rows().oneOrMore()
                .cells().one().val()
                .cells().one().val()
                .apply(syntax);
    }
}
