package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;


/**
 * Task 75: each row — anchor VAL with RT*->REC; subsequent cells fill from the
 * nearest non-blank cell to the left when blank, otherwise plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_75/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask75Test}
 */
class AtpTask75Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF   = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec LT_NBLANK  = ItemFilterConditionSpec.and(
            FilterTerm.LeftOf.INSTANCE, FilterTerm.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "75"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(
                                                ActionSpec.fill("", ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, LT_NBLANK))
                                        ),
                                        AtomicContentSpec.val()
                                ))
                        )
                )
        );
    }
}
