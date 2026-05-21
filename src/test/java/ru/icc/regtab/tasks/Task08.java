package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 08 (Foofah exp0_10): unpivot wide clothing × colors — первая колонка (изделие), остальные — цвета в заголовке.
 * <p>
 * Как {@link Task04}: {@code rec} ставит якорь на каждую value-ячейку цвета, провайдер берёт метку из той же строки;
 * {@link AnchorAttributeAtPosition}{@code (1)} даёт порядок колонок эталона: изделие, цвет.
 */
public final class Task08 extends TaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW =
            ProviderSpec.any((a, c) -> c.sameRow(a), 1);

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().oneOrMore().skip()
                .rows().oneOrMore()
                .cells().one().val()
                .cells().oneOrMore().val()
                .actions().rec(FIRST_IN_SAME_ROW)
                .apply(syntax);
    }
}
