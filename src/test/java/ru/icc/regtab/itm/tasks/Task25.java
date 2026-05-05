package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 25 (Foofah exp0_34): fold consecutive rows with the same id into one wide record. The id cell is the
 * {@code rec} anchor; {@code O_suffix} appends the account column with delimiter {@code "/"}; {@code O_rec}
 * collects same-row value cells strictly to the right of the account column; {@code O_concat} merges tails from
 * rows below that match the id column. Post-step splits the merged id/account field on {@code "/"} to match
 * benchmark column layout.
 */
public final class Task25 extends TaskBase {

    private static final String SEP = "/";

    /** One value cell immediately to the right of the id anchor on the same row (account). */
    private static final ProviderSpec RIGHT_SAME_ROW =
            ProviderSpec.val((a, c) -> c.is.rightOf(a).sameRow(), 1);

    /** Same-row value cells to the right of the account column (keyword + metrics). */
    private static final ItemFilterCondition SAME_ROW_AFTER_ACCOUNT =
            (a, c) -> c.is.in.sameRow(a) && c.cell().col() > a.cell().col() + 1;

    private static final ItemFilterCondition SAME_GROUP_BELOW =
            (a, c) -> c.is.below(a).sameCol() && c.has.sameStr(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().suffix(SEP, RIGHT_SAME_ROW).rec(SAME_ROW_AFTER_ACCOUNT)
                .concat(SAME_GROUP_BELOW)
                .cells().one().val()
                .cells().oneOrMore().val()
                .apply(syntax);
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new DelimitedFieldSplit(SEP).apply(actual);
    }
}
