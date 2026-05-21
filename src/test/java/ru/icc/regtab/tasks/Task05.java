package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 05: unpivot a matrix (corner + date header row, blank spacer row, data rows).
 * <p>
 * ITM {@code rec} lists the measure cell first, then same-row label and same-column header;
 * expected format is product, date, value — anchor (measure) moved to last column via {@link AnchorAttributeAtPosition}{@code (2)}.
 */
public final class Task05 extends TaskBase {

    private static final ProviderSpec UNPIVOT_ROW_KEY =
            ProviderSpec.any((a, c) -> c.sameRow(a), 1);
    private static final ProviderSpec UNPIVOT_COL_KEY =
            ProviderSpec.any((a, c) -> c.sameCol(a), 1);

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
                .rows().one()
                .cells().oneOrMore().skip()
                .rows().oneOrMore()
                .cells().one().val()
                .cells().oneOrMore().val()
                .actions().rec(UNPIVOT_ROW_KEY, UNPIVOT_COL_KEY)
                .apply(syntax);
    }
}
