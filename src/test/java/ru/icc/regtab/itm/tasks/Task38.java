package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 38 (Foofah exp0_agriculture): forward-fill the value column — blanks copy the nearest non-blank above
 * in the same column. {@code O_rec} on the first column collects the row; for blank cells {@code when(textBlank).val()}
 * attaches {@code O_fill} (see {@link #FILL_FROM_ABOVE}).
 */
public final class Task38 extends TaskBase {

    private static final ItemFilterCondition SAME_ROW = (a, c) -> c.sameRow(a);

    /**
     * {@code O_fill}: one candidate strictly above the anchor in the same column; {@link TraversalOrder#REVERSE_ROW_MAJOR}
     * picks the nearest row above first (same triple as {@code rec} providers).
     */
    private static final ProviderSpec FILL_FROM_ABOVE =
            ProviderSpec.any((a, c) -> c.above(a).sameCol(), TraversalOrder.REVERSE_ROW_MAJOR, 1);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().rec(SAME_ROW)
                .cells().one().val()
                .cells().one().when(Cell::textBlank).val()
                .actions().fill(FILL_FROM_ABOVE)
                .otherwise().val()
                .apply(syntax);
    }
}
