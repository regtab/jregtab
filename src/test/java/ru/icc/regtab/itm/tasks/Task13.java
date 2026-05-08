package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 13 (Foofah exp0_17): header row defines five attribute names (first five columns); remaining header cells are
 * skipped. Data rows pair each value with the header attribute in the same column via {@code avp(sameCol)}; the first
 * cell is a {@code rec} anchor that also pulls values from columns 2, 4, 1, and 3 (in that order), so the record
 * column order is Header1, Header3, Header5, Header2, Header4 — all named attributes, no anonymous schema slots.
 */
public final class Task13 extends TaskBase {

    private static final ItemFilterCondition AVP_SAME_COL = (a, c) -> c.sameCol(a);

    private static final ProviderSpec SAME_ROW_COL2 =
            ProviderSpec.val((a, c) -> c.sameRow(a) && c.col(2));
    private static final ProviderSpec SAME_ROW_COL4 =
            ProviderSpec.val((a, c) -> c.sameRow(a) && c.col(4));
    private static final ProviderSpec SAME_ROW_COL1 =
            ProviderSpec.val((a, c) -> c.sameRow(a) && c.col(1));
    private static final ProviderSpec SAME_ROW_COL3 =
            ProviderSpec.val((a, c) -> c.sameRow(a) && c.col(3));

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().exactly(5).attr()
                .cells().oneOrMore().skip()
                .rows().oneOrMore()
                .cells().one().val()
                .actions()
                .avp(AVP_SAME_COL)
                .rec(SAME_ROW_COL2, SAME_ROW_COL4, SAME_ROW_COL1, SAME_ROW_COL3)
                .cells().exactly(4).val()
                .actions().avp(AVP_SAME_COL)
                .cells()
                .oneOrMore().skip()
                .apply(syntax);
    }
}
