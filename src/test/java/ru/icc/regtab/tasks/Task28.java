package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 28 (Foofah exp0_40): fold a rectangular value table into one wide row — all cell values in row-major order.
 * <p>
 * {@code subtables().one()} keeps the whole sheet as one subtable. Top-left value is the {@code rec} anchor;
 * {@code sameSubtable} collects every other value cell. First row: anchor plus {@code oneOrMore} trailing cells;
 * further rows: {@code oneOrMore} cells each.
 */
public final class Task28 extends TaskBase {

    private static final ItemFilterCondition REC_REST_OF_SUBTABLE =
            (a, c) -> c.sameSubtable(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().val()
                .actions().rec(REC_REST_OF_SUBTABLE)
                .cells().oneOrMore().val()
                .rows().oneOrMore()
                .cells().oneOrMore().val()
                .apply(syntax);
    }
}
