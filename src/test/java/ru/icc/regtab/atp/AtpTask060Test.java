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
 * Task 60: column-header subtable followed by repeating data subtables — anchor
 * VAL with RT*->REC and inherited COL->AVP; subsequent cells fill forward from above.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_060/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask060Test}
 */
class AtpTask060Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF      = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL      = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec ABOVE_NBLANK  = ItemFilterConditionSpec.and(
            FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "060"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));
        ActionSpec fill   = ActionSpec.fill("", ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, ABOVE_NBLANK));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        colAvp,
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(colAvp, fill),
                                        AtomicContentSpec.val(colAvp)
                                ))
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null)
                        )
                )
        );
    }
}
