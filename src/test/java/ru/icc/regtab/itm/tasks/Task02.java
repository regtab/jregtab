package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;


/**
 * Task 02: stack blocks (fund / activity / dated amounts) into a 4-column recordset.
 * <p>
 * Tags mark header cells; {@code rec} pulls L1/L2 from the same subtable and the amount from the same row.
 * Post-processing: {@link WhitespaceNormalization} and {@link AnchorAttributeAtPosition} (2) so the schema matches expected key order.
 */
public final class Task02 extends TaskBase {

    private static final ProviderSpec L1_L2_SAME_SUBTABLE = ProviderSpec.any(
            (a, c) -> c.sameSubtable(a) && (c.hasTag("#L1") || c.hasTag("#L2")));

    private static final ProviderSpec SAME_ROW_REST = ProviderSpec.any(
            (a, c) -> c.sameRow(a), 1);

    @Override
    protected Recordset transformActual(Recordset actual) {
        Recordset n = new WhitespaceNormalization().apply(actual);
        return new AnchorAttributeAtPosition(2).apply(n);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val().setTag("#L1")
                .cells().one().skip()
                .rows().one()
                .cells().one().val().setTag("#L2")
                .cells().one().skip()
                .rows().oneOrMore()
                .cells().one().check(c -> !c.textBlank()).val()
                .actions().rec(L1_L2_SAME_SUBTABLE, SAME_ROW_REST)
                .cells().one().val()
                .rows().zeroOrMore()
                .cells().one().skip()
                .cells().one().skip()
                .apply(syntax);
    }
}
