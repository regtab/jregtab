package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 42 (Foofah exp0_potters_wheel_fold). Широкая строка: имя, затем ячейки {@code Предмет:балл};
 * результат — записи с колонками {@code ""} (имя, из {@code O_avp("")}), {@code Math}, {@code French} из атрибутов compound.
 * <p>
 * Первый столбец — якорь {@code O_rec}: все ячейки строго правее в той же строке. Два предметных столбца задаются
 * горизонтальным повтором {@link TablePattern.SubrowsCardinalityBuilder#exactly(int)} (в данных по два предмета).
 * В каждой такой ячейке compound {@code attr ":" val} и {@code O_avp} на атрибут в той же ячейке.
 */
public final class Task42 extends TaskBase {

    private static final ItemFilterCondition RIGHT_OF_SAME_ROW = (a, c) -> c.rightOf(a).sameRow();

    private static final ItemFilterCondition ATTR_SAME_CELL_AS_VALUE = (a, c) -> c.sameCell(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().val()
                .actions().avp("").rec(RIGHT_OF_SAME_ROW)
                .subrows().exactly(2)
                .cells().one().compound()
                .attr()
                .sep(":")
                .val()
                .cells()
                .actions().avp(ATTR_SAME_CELL_AS_VALUE)
                .apply(syntax);
    }
}
