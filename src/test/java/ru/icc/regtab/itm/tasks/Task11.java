package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 11: wide table with dates as columns; unpivot to (item, date, value) for non-blank cells only.
 */
public final class Task11 extends TaskBase {

    private static final ItemFilterCondition FIRST_IN_ROW = (a, c) ->
            c.sameRow(a);

    private static final ItemFilterCondition FIRST_IN_COL = (a, c) ->
            c.sameCol(a);

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
                .cells().oneOrMore()
                .when(Cell::textBlank).skip()
                .otherwise().val()
                .actions().rec(
                        ProviderSpec.of(FIRST_IN_ROW, 1),
                        ProviderSpec.of(FIRST_IN_COL, 1))
                .apply(syntax);
    }
}
