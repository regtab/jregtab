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
 * Task 67: header ATTR row then data rows — first cell anchors RT*->REC with
 * inherited COL->AVP and optional fill-from-above; remaining cells fill or copy
 * with inherited COL->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_67/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask67Test}
 */
class AtpTask67Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF     = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL     = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec ABOVE_NBLANK = ItemFilterConditionSpec.and(
            FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "67"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));
        ActionSpec rec    = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF));
        ActionSpec fill   = ActionSpec.fill("", ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, ABOVE_NBLANK));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(colAvp, rec, fill),
                                        AtomicContentSpec.val(colAvp, rec)
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(colAvp, fill),
                                        AtomicContentSpec.val(colAvp)
                                ))
                        )
                )
        );
    }
}
