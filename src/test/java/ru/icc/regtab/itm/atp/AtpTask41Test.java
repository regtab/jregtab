package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task41.
 */
class AtpTask41Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();
    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() {
        return "41";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec pairValSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.fill("", ProviderSpec.ctxAttr("")), ActionSpec.rec(ProviderSpec.val(1, SAME_CELL), ProviderSpec.val(1, RIGHT_OF))),
                CompoundContentSpec.Segment.of("", AtomicContentSpec.val())
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), pairValSpec),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(),
                                        AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(1, RIGHT_OF), ProviderSpec.ctxAttr(""))
                                        )
                                ),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        )
                )
        );
    }

}
