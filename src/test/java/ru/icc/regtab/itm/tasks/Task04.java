package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 04: unpivot matrix (header row, data rows with machine label + values).
 * <p>
 * ITM {@code rec} lists value first, then same-row machine; {@link AnchorAttributeAtPosition}{@code (1)} yields machine, value.
 */
public final class Task04 extends TaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW =
            ProviderSpec.of((a, c) -> c.is.in.sameRow(a), 1);

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
