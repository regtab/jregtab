package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 27 (Foofah exp0_37): stacked blocks of columnar labels; each block is one subtable with a header row,
 * a blank row, then nine label rows. The first cell is the {@code rec} anchor; {@code O_rec} collects value items
 * in the same column within the subtable below the anchor (skipping the blank row because it emits no items).
 * <p>
 * Subtable height is fixed by the pattern ({@code 1 + 1 + 9} rows); the engine infers an 11-row stride from that
 * sequence so subtable boundaries do not depend on the header cell text.
 */
public final class Task27 extends TaskBase {

    private static final ItemFilterCondition SAME_SUBTABLE_BELOW_SAME_COL =
            (a, c) -> c.sameSubtable(a) && c.below(a).sameCol();

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(SAME_SUBTABLE_BELOW_SAME_COL)
                .rows().one()
                .cells().one().skip()
                .rows().exactly(9)
                .cells().one().val()
                .apply(syntax);
    }
}
