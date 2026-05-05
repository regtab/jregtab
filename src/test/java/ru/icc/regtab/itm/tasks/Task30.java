package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 30 (Foofah exp0_43): each logical record is one column × four rows (name and three lines below);
 * fold into one wide row per record.
 * <p>
 * {@code subtables().oneOrMore()} with row pattern {@code one() + exactly(3)} yields stride 4 rows per subtable.
 * {@code rec} on the top cell pulls the next three value cells below in the same column (cardinality 3).
 */
public final class Task30 extends TaskBase {

    private static final ProviderSpec BELOW_SAME_COL =
            ProviderSpec.val(
                    (a, c) -> c.is.below(a).sameCol() && c.is.in.sameSubtable(a));

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(BELOW_SAME_COL)
                .rows().exactly(3)
                .cells().one().val()
                .apply(syntax);
    }
}
