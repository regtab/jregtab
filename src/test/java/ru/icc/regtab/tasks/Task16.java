package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;

/**
 * Task 16 (Foofah exp0_22): two columns (label, value); consecutive rows with the same label are folded into one
 * wide record (label + all values). Each row’s first cell is a {@code rec} anchor; {@code rec} pulls the cell to the
 * right on the same row. {@code concat} merges {@code rec} tails from later rows with the same label (below, same column).
 */
public final class Task16 extends TaskBase {

    private static final ItemFilterCondition REC_RIGHT = (a, c) -> c.rightOf(a).sameRow();

    /** Later row, same column (label), same string as anchor — another anchor in the same label run. */
    private static final ItemFilterCondition CONCAT_SAME_LABEL_BELOW =
            (a, c) -> c.below(a).sameCol() && c.sameStr(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().rec(REC_RIGHT)
                .concat(CONCAT_SAME_LABEL_BELOW)
                .cells().one().val()
                .apply(syntax);
    }
}
