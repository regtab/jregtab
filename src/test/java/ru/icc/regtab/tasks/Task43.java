package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 43 (Foofah exp0_potters_wheel_fold_2). Как {@link Task42}, но три предметных столбца после имени
 * ({@code Math}, {@code French}, {@code History}): {@code subrows().exactly(3)}.
 * <p>
 * Черновик с {@code cells().oneOrMore().compound()} недоступен в API — вместо этого горизонтальное повторение
 * подстрок (см. {@link Task42}). Порядок: {@code avp("").rec(...)}; у предиката правее строки — {@code sameRow()}.
 */
public final class Task43 extends TaskBase {

    private static final ItemFilterCondition RIGHT_OF_SAME_ROW = (a, c) -> c.rightOf(a).sameRow();

    private static final ItemFilterCondition ATTR_SAME_CELL_AS_VALUE = (a, c) -> c.sameCell(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().avp("").rec(RIGHT_OF_SAME_ROW)
                .subrows().exactly(3)
                .cells().one().compound()
                .attr()
                .sep(":")
                .val()
                .cells()
                .actions().avp(ATTR_SAME_CELL_AS_VALUE)
                .apply(syntax);
    }
}
