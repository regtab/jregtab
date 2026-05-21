package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 31 (Foofah exp0_44): each block is five label lines in one column, then a blank separator row;
 * fold into one output row of five values.
 * <p>
 * Row pattern {@code one() + exactly(4) + one()} gives stride 6 rows per subtable (anchor + four fields + blank skip).
 * {@code rec} on the first cell collects the next four values below (cardinality 4).
 */
public final class Task31 extends TaskBase {

    private static final ProviderSpec BELOW_SAME_COL =
            ProviderSpec.val(
                    (a, c) -> c.below(a).sameCol() && c.sameSubtable(a));

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(BELOW_SAME_COL)
                .rows().exactly(4)
                .cells().one().val()
                .rows().one()
                .cells().one().skip()
                .apply(syntax);
    }
}
