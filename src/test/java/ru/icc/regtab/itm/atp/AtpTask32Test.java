package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * ATP equivalent of Fluent API Task32.
 */
class AtpTask32Test extends AtpTaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW =
            ProviderSpec.val(1, (a, c) -> c.is.in.sameRow(a));

    private static final ProviderSpec FIRST_IN_SAME_COL =
            ProviderSpec.val(1, (a, c) -> c.is.in.sameCol(a));

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ConditionalContentSpec BLANK_SKIP_OTHERWISE_VAL_WITH_REC = new ConditionalContentSpec(
            BLANK,
            AtomicContentSpec.skip(),
            AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COL)));

    @Override
    protected String taskId() {
        return "32";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), BLANK_SKIP_OTHERWISE_VAL_WITH_REC)
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
