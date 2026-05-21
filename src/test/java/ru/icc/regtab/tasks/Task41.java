package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 41 (Foofah exp0_potters_wheel_divide). Two-column rows use {@code fill} with empty context aux plus
 * same-cell and right-neighbour values to produce one field (e.g. {@code /Anna/Davis}); single-column blocks use
 * {@code suffix} with two empty aux items for {@code //}.
 */
public final class Task41 extends TaskBase {

    private static final ItemFilterCondition SAME_CELL = (a, c) -> c.sameCell(a);
    private static final ItemFilterCondition RIGHT_SAME_ROW = (a, c) -> c.rightOf(a).sameRow();

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().check(c -> !c.textBlank()).val()
                .actions().fill("/",
                        ProviderSpec.aux(""),
                        ProviderSpec.val(SAME_CELL),
                        ProviderSpec.val(RIGHT_SAME_ROW))
                .rec()
                .cells().one().check(c -> !c.textBlank()).val()
                .rows().zeroOrOne()
                .cells().one().check(c -> !c.textBlank()).val()
                .actions().suffix("/",
                        ProviderSpec.aux(""),
                        ProviderSpec.aux("")
                ).rec()
                .cells().one().check(Cell::textBlank).skip()
                .apply(syntax);
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new DelimitedFieldSplit("/").apply(actual);
    }
}
