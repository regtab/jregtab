package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 38: flat table with forward-fill — each row has a same-row REC anchor,
 * a plain value, and a third cell that fills from above when blank.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_38/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask38Test}
 */
class AtpTask38Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec ABOVE       = ItemFilterConditionSpec.above();

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() {
        return "38";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBROW)))),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(ActionSpec.fill("", ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, ABOVE))),
                                        AtomicContentSpec.val()))
                        )
                )
        );
    }
}
