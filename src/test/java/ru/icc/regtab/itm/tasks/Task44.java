package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 44 (Foofah exp0_potters_wheel_merge_split). Черновик паттерна — см. {@link #buildItm(TableSyntax)}.
 */
public final class Task44 extends TaskBase {

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().oneOrMore()
                .cells().one().check(c -> !c.textBlank()).val()
                .actions().rec(ProviderSpec.of((a, c) -> c.sameRow(a), 1))
                .cells().one().check(c -> !c.textBlank()).val()
                .cells().one().check(Cell::textBlank).skip()
                .rows().zeroOrOne()
                .cells().exactly(2).check(Cell::textBlank).skip()
                .cells().one().check(c -> !c.textBlank()).compound()
                .val().sep(",").val()
                .actions().rec((a, c) -> c.sameCell(a))
                .apply(syntax);
    }
}
