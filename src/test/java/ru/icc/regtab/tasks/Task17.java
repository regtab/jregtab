package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;

/**
 * Task 17: extract address blocks (company + address lines, split by blank rows).
 * <p>
 * Each block: company row, 2–4 address rows, optional blank separator ({@code rows().zeroOrOne()}
 * with one {@code skip()} cell; application accepts any content in that cell, boundary inference treats it as blank-only).
 * Rec collects non-blank cells below anchor in same subtable/column.
 */
public final class Task17 extends TaskBase {

    private static final ProviderSpec BELOW_SAME_SUBTABLE =
            ProviderSpec.any((a, c) -> c.below(a).sameSubtable() 
            && c.below(a).sameCol());

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
