package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 03: unpivot wide table (one key column + two value columns).
 * <p>
 * ITM {@code rec} lists value first, then same-row key; {@link AnchorAttributeAtPosition}{@code (1)} yields key, value.
 */
public final class Task03 extends TaskBase {

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
                .rows().oneOrMore()
                .cells().one().val()
                .cells().exactly(2).val()
                .actions().rec(FIRST_IN_SAME_ROW)
                .apply(syntax);
    }
}
