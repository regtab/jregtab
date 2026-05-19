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
import ru.icc.regtab.itm.interpret.WhitespaceNormalization;

/**
 * ATP equivalent of RTL Task02: two generic header rows, data rows look up
 * both headers via sameSubcol (card 2) and same-row value via sameSubrow (card 1).
 */
class AtpTask02Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());
    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ProviderSpec FIRST_TWO_IN_SAME_SUBCOL = ProviderSpec.of(2, (a, c) -> c.sameSubcol(a));
    private static final ProviderSpec FIRST_IN_SAME_SUBROW = ProviderSpec.of(1, (a, c) -> c.sameSubrow(a));

    @Override
    protected String taskId() {
        return "02";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(FIRST_TWO_IN_SAME_SUBCOL, FIRST_IN_SAME_SUBROW)
                                )),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.skip()
                        )
                )
        ).withTransformations(new WhitespaceNormalization(), new AnchorAttributeAtPosition(2));
    }
}
