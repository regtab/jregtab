package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * ATP equivalent of Fluent API Task14.
 */
class AtpTask14Test extends AtpTaskBase {

    private static final ProviderSpec FIRST_SAME_SUBTABLE_COL0 =
            ProviderSpec.of(1, (a, c) -> c.is.in.sameSubtable(a) && c.is.in.col(0));

    private static final ProviderSpec FIRST_SAME_SUBTABLE_COL1 =
            ProviderSpec.of(1, (a, c) -> c.is.in.sameSubtable(a) && c.is.in.col(1));

    private static final ProviderSpec SAME_ROW =
            ProviderSpec.of(2, (a, c) -> c.is.in.sameRow(a));

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    @Override
    protected String taskId() {
        return "14";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(FIRST_SAME_SUBTABLE_COL0, FIRST_SAME_SUBTABLE_COL1, SAME_ROW)
                                ))
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(4).apply(actual);
    }
}
