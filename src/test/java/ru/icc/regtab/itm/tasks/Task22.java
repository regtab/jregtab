package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.CellDerivedItemProvider;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 22 (Foofah exp0_29): crosstab-style blocks — 2 rows per entity (sum row + count row), unpivot columns 2–5
 * into alternating sum/count values in one wide record per entity.
 * <p>
 * Stride {@code 1 + rows().exactly(1)} = 2 rows per subtable. Anchor on the entity cell (column 0); {@code rec}
 * collects value items in columns 2–5 within the same subtable. {@link TraversalOrder#COLUMN_MAJOR} orders
 * (sum, count) pairs per column, matching the benchmark output.
 */
public final class Task22 extends TaskBase {

    private static final ItemFilterCondition SAME_SUBTABLE_COLS_2_TO_5 =
            (a, c) -> c.sameSubtable(a) && c.cols.from(2).to(5);

    private static final ProviderSpec REC_COLS_2_TO_5_COLUMN_MAJOR =
            ProviderSpec.val(SAME_SUBTABLE_COLS_2_TO_5, TraversalOrder.COLUMN_MAJOR, CellDerivedItemProvider.UNBOUNDED);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(REC_COLS_2_TO_5_COLUMN_MAJOR)
                .cells().one().skip()
                .cells().oneOrMore().val()
                .rows().exactly(1)
                .cells().exactly(2).skip()
                .cells().oneOrMore().val()
                .apply(syntax);
    }
}
