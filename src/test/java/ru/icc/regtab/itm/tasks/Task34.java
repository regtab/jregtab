package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 34 (Foofah exp0_47): single column split into blocks of five stacked values per output row
 * (no blank row between blocks — unlike {@link Task31}).
 * <p>
 * Row pattern {@code one() + exactly(4)} yields stride 5 rows per subtable.
 */
public final class Task34 extends TaskBase {

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
                .rows().exactly(4)
                .cells().one().val()
                .apply(syntax);
    }
}
