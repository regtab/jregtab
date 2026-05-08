package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * Task 14 (Foofah exp0_18): stacked vendor blocks — each block has a header row (vendor id, name, blank third column)
 * and one or more product rows. {@code rec} on the price cell pulls the header’s two cells from columns 0 and 1 in the
 * same subtable plus the two non-price cells on the same row; {@link AnchorAttributeAtPosition}{@code (4)} moves the
 * anchor value (price) to the last column to match the expected wide schema.
 */
public final class Task14 extends TaskBase {

    private static final ProviderSpec FIRST_SAME_SUBTABLE_COL0 =
            ProviderSpec.val((a, c) -> c.sameSubtable(a) && c.col(0), 1);

    private static final ProviderSpec FIRST_SAME_SUBTABLE_COL1 =
            ProviderSpec.val((a, c) -> c.sameSubtable(a) && c.col(1), 1);

    /** Product id and name: the two value cells on the same row as the anchor (price), in row-major order. */
    private static final ProviderSpec SAME_ROW =
            ProviderSpec.val((a, c) -> c.sameRow(a), 2);

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(4).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().exactly(2).val()
                .cells().one().check(Cell::textBlank).skip()
                .rows().oneOrMore()
                .cells().exactly(2).check(c -> !c.textBlank()).val()
                .cells().one().val()
                .actions().rec(
                        FIRST_SAME_SUBTABLE_COL0,
                        FIRST_SAME_SUBTABLE_COL1,
                        SAME_ROW)
                .apply(syntax);
    }
}
