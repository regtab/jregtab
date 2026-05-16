package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubrowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;

/**
 * ATP equivalent of Fluent API Task11.
 */
class AtpTask11Test extends AtpTaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW = ProviderSpec.of(1, (a, c) -> c.sameRow(a));
    private static final ProviderSpec FIRST_IN_SAME_COL = ProviderSpec.of(1, (a, c) -> c.sameCol(a));

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ConditionalContentSpec BLANK_SKIP_OTHERWISE_VAL_WITH_REC = new ConditionalContentSpec(
            BLANK,
            AtomicContentSpec.skip(),
            AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COL)));

    @Override
    protected String taskId() {
        return "11";
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
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.val()),
                                        CellPattern.of(Quantifier.oneOrMore(), BLANK_SKIP_OTHERWISE_VAL_WITH_REC)
                                )
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(2));
    }
}
