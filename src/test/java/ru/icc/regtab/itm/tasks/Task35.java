package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 35 (Foofah exp0_48): stacked company blocks in one column — {@code *Company …} row starts a block;
 * {@code rec} on that row collects all following non-company lines in the same column within the subtable.
 * Subtable boundaries follow rows that match the first row type ({@code *Company}).
 * <p>
 * Asterisks are stripped from the company cell text for output.
 */
public final class Task35 extends TaskBase {

    private static final ProviderSpec BELOW_SAME_COL =
            ProviderSpec.val(
                    (a, c) -> c.below(a).sameCol() && c.sameSubtable(a));

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one()
                .check(c -> c.text().contains("*Company"))
                .val(c -> c.text().replaceAll("\\*", ""))
                .actions().rec(BELOW_SAME_COL)
                .rows().oneOrMore()
                .cells().one()
                .check(c -> !c.text().contains("*Company"))
                .val()
                .apply(syntax);
    }
}
