package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import static ru.icc.regtab.atp.spec.ItemFilterConditionSpec.and;


/**
 * Task 86: implicit ATTR header subtable; repeating explicit subtables with row-level
 * COL-&gt;AVP, anchor VAL (RT*, R+1 &amp; C2)-&gt;REC, and a 'CtxAttr'-labelled continuation VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_086/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask086Test}
 */
class AtpTask086Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec R1_C2    =
            and(new FilterTerm.RowOffset(1), new FilterTerm.ColExact(2));

    @Override
    protected String taskId() { return "086"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(3), AtomicContentSpec.attr())
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        colAvp,
                                        ActionSpec.rec(
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF),
                                                ProviderSpec.val(R1_C2)
                                        )
                                )),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val(colAvp))
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("CtxAttr")))
                        )
                )
        );
    }
}
