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
 * ATP equivalent of Fluent API Task44.
 */
class AtpTask44Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec SAME_CELL   = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() {
        return "44";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec commaPair = CompoundContentSpec.of(
                AtomicContentSpec.val(),
                CompoundContentSpec.Segment.of(",", AtomicContentSpec.val(
                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL))
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(1, SAME_SUBROW))
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.exactly(2), null),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), commaPair)
                        )
                )
        );
    }
}
