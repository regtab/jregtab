package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 45: last name in column 0, first name(s) in column 1 (comma-separated); unpivot to one row per
 * (last name, first name). Each first-name token is a {@code rec} anchor; provider picks the surname cell
 * in column 0 of the same row.
 */
public final class Task45 extends TaskBase {

    private static final ItemFilterCondition SAME_ROW_COL0 = (a, c) ->
            c.sameRow(a) && c.col(0);

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
                .cells().one().delimited(",").val()
                .actions().rec(SAME_ROW_COL0)
                .apply(syntax);
    }
}
