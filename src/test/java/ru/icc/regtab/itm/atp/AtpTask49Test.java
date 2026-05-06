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
 * ATP equivalent of Fluent API Task49.
 */
class AtpTask49Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    private static final ProviderSpec FIRST_SAME_ROW = ProviderSpec.val(1, (a, c) -> c.sameRow(a));
    private static final ProviderSpec FIRST_SAME_COL = ProviderSpec.val(1, (a, c) -> c.sameCol(a));

    @Override
    protected String taskId() {
        return "49";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.one()),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(FIRST_SAME_ROW, FIRST_SAME_COL)
                                ))
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
