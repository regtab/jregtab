package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 37 (Foofah exp0_51): unpivot qualification date columns — corner skipped, {@code Qual 1…} headers,
 * per-person rows with blanks skipped; {@code rec} on each date cell collects name (same row) and column header (same column).
 * <p>
 * {@link AnchorAttributeAtPosition}{@code (2)}: эталон {@code $a_0} имя, {@code $a_1} квалификация, {@code $a_2} дата.
 */
public final class Task37 extends TaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW =
            ProviderSpec.val((a, c) -> c.sameRow(a), 1);

    private static final ProviderSpec FIRST_IN_SAME_COL =
            ProviderSpec.val((a, c) -> c.sameCol(a), 1);

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().skip()
                .cells().oneOrMore().val()
                .rows().oneOrMore()
                .cells().one().val()
                .cells().oneOrMore().when(Cell::textBlank).skip().otherwise().val()
                .actions().rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COL)
                .apply(syntax);
    }
}
