package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 07 (Foofah exp0_8): unpivot month columns — каждая числовая ячейка месяца (якорь) получает {@code rec}:
 * первые три value в той же строке (Year, CatNum, Comments) и один заголовок месяца в той же колонке.
 * <p>
 * После интерпретации якорь оказывается в {@code $a_0}; эталон — Year, CatNum, Comments, месяц, значение —
 * поэтому {@link AnchorAttributeAtPosition}{@code (4)} сдвигает значение якоря в последний столбец.
 */
public final class Task07 extends TaskBase {

    private static final ProviderSpec FIRST_THREE_IN_SAME_ROW =
            ProviderSpec.val((a, c) -> c.sameRow(a), 3);

    private static final ProviderSpec FIRST_IN_SAME_COLUMN =
            ProviderSpec.val((a, c) -> c.sameCol(a), 1);

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(4).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().exactly(3).skip()
                .cells().oneOrMore().val()
                .rows().oneOrMore()
                .cells().exactly(3).val()
                .cells().oneOrMore().val()
                .actions().rec(FIRST_THREE_IN_SAME_ROW, FIRST_IN_SAME_COLUMN)
                .apply(syntax);
    }
}
