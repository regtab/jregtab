package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 32 (Foofah exp0_45): unpivot house columns — corner skipped, headers in row 0, per-person rows with blanks skipped;
 * {@code rec} anchor on each non-blank house cell collects name (same row) and house label (same column).
 * <p>
 * {@link AnchorAttributeAtPosition}{@code (2)}: эталон {@code $a_0} имя, {@code $a_1} дом, {@code $a_2} значение.
 */
public final class Task32 extends TaskBase {

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
