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

    private static final ProviderSpec FIRST_IN_SAME_ROW = ProviderSpec.of(1, (a, c) -> c.sameSubrow(a));
    private static final ProviderSpec FIRST_IN_SAME_COL = ProviderSpec.of(1, (a, c) -> c.sameSubcol(a));

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

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
                                        CellPattern.of(Quantifier.oneOrMore(),
                                new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.skip(),
                                        AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COL))))
                                )
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(2));
    }
}
