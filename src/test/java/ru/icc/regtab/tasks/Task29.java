package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.recordset.Recordset;

/**
 * Task 29 (Foofah exp0_41): wide rows with a fixed six-column prefix and repeating four-column groups
 * (material, amount, material, batch). Each group is unpivoted to one output row: prefix + four fields.
 * <p>
 * {@link TablePattern.RowCellsBuilder#subrows()} repeats the inner cell pattern horizontally on the same
 * physical row; {@link AnchorAttributeAtPosition}{@code (6)} places the rec anchor column at attribute index 6.
 */
public final class Task29 extends TaskBase {

    private static final ProviderSpec FIRST_SIX_SAME_ROW =
            ProviderSpec.val((a, c) -> c.sameRow(a), 6);

    // All value cells in the same subrow as the rec anchor (anchor excluded by Υ semantics); here that is three per group.
    private static final ProviderSpec SAME_SUBROW =
            ProviderSpec.val((a, c) -> c.sameSubrow(a));

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(6).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().exactly(6).val()
                .subrows().oneOrMore()
                .cells().one().val()
                .actions().rec(FIRST_SIX_SAME_ROW, SAME_SUBROW)
                .cells().exactly(3).val()
                .apply(syntax);
    }
}
