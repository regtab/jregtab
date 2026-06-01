package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;


/**
 * Task 64: two-row-type table — first row anchors BW*->REC with reverse-row AVP;
 * subsequent rows carry only the reverse-row AVP; both rows end with an ATTR cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_064/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask064Test}
 */
class AtpTask064Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW    = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "064"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec revRowAvp = ActionSpec.avp(ProviderSpec.attr(TraversalOrder.REVERSE_ROW_MAJOR, SAME_ROW));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW)),
                                        revRowAvp
                                )),
                                CellPattern.of(AtomicContentSpec.attr())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(revRowAvp)),
                                CellPattern.of(AtomicContentSpec.attr())
                        )
                )
        );
    }
}
