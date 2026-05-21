package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 41: repeated subtables each with two optional rows — a compound fill+REC
 * row and a right-then-context REC row — for key-value pair extraction.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_41/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask41Test}
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
                                                ActionSpec.rec(ProviderSpec.val(1, RIGHT_OF), ProviderSpec.ctxVal(""))
                                        )
                                ),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        )
                )
        );
    }

}
