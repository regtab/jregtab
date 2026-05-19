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
 * ATP equivalent of Fluent API Task02.
 */
class AtpTask02Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());
    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ProviderSpec L1_L2_SAME_SUBTABLE = ProviderSpec.of((a, c) ->
            c.sameSubtable(a) && (c.hasTag("#L1") || c.hasTag("#L2")));

    private static final ProviderSpec SAME_ROW_REST = ProviderSpec.of(1, (a, c) -> c.sameSubrow(a));

    @Override
    protected String taskId() {
        return "02";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.valTagged("#L1")),
                                CellPattern.skip()
                        ),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.valTagged("#L2")),
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(L1_L2_SAME_SUBTABLE, SAME_ROW_REST)
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
