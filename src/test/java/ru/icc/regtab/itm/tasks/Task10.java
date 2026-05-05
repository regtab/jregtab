package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 10 (Foofah exp0_12): multiple stacked blocks on one sheet; blocks are separated by a fully blank row,
 * and there is no separator row after the last block.
 * <p>
 * Each block: zero or more template rows (prefix and tail use {@code skip}; columns that must stay empty use
 * {@code check(Cell::textBlank).skip()}), then one data row where {@code rec} with a {@code sameRow} provider
 * collects values from that row. Block boundaries are a row of eight blank cells
 * ({@code exactly(8).check(Cell::textBlank).skip()}). Template rows use {@code rows().zeroOrMore()} so a block
 * with only the data row (no templates) still matches.
 */
public final class Task10 extends TaskBase {

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().zeroOrMore()
                .cells().exactly(4).skip()
                .cells().one().check(Cell::textBlank).skip()
                .cells().one().skip()
                .cells().exactly(2).skip()
                .rows().one()
                .cells().one().val()
                .actions().rec((a, c) -> c.is.in.sameRow(a))
                .cells().oneOrMore().val()
                .rows().zeroOrOne()
                .cells().oneOrMore().check(Cell::textBlank).skip()
                .apply(syntax);
    }
}
