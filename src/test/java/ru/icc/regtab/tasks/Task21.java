package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 21 (Foofah exp0_28): stacked address blocks, 3 rows × 2 columns per block — unpivot each column to a record
 * (name, street, city/state). Each block is one subtable (stride 3); first-row cells are {@code rec} anchors;
 * provider collects value cells strictly below in the same column within the subtable.
 * <p>
 * Ground-truth CSV trims/collapses whitespace; {@link WhitespaceNormalization} aligns raw cell text with expected.
 */
public final class Task21 extends TaskBase {

    private static final ItemFilterCondition BELOW_SAME_COL_SAME_SUBTABLE =
            (a, c) -> c.sameSubtable(a) && c.below(a).sameCol();

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new WhitespaceNormalization().apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().oneOrMore().val()
                .actions().rec(BELOW_SAME_COL_SAME_SUBTABLE)
                .rows().exactly(2)
                .cells().oneOrMore().val()
                .apply(syntax);
    }
}
