package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 17: extract address blocks (company + address lines, split by blank rows).
 * <p>
 * Each block: company row, 2–4 address rows, optional blank separator ({@code rows().zeroOrOne()}
 * with one {@code skip()} cell; application accepts any content in that cell, boundary inference treats it as blank-only).
 * Rec collects non-blank cells below anchor in same subtable/column.
 */
public final class Task17 extends TaskBase {

    private static final ProviderSpec BELOW_SAME_SUBTABLE =
            ProviderSpec.of((a, c) -> c.is.below(a).sameSubtable() 
            && c.is.below(a).sameCol());

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(BELOW_SAME_SUBTABLE)
                .rows().oneOrMore()
                .cells().one().check(c -> !c.textBlank()).val()
                .rows().zeroOrOne()
                .cells().one().skip()
                .apply(syntax);
    }
}
