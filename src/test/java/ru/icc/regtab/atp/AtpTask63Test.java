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
 * Task 63: data rows with RT*->REC anchor and inherited reverse-column AVP,
 * followed by a single ATTR header row below the data.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_63/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask63Test}
 */
class AtpTask63Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "63"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec revColAvp = ActionSpec.avp(ProviderSpec.attr(TraversalOrder.REVERSE_ROW_MAJOR, SAME_COL));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        revColAvp,
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.zeroOrMore(), AtomicContentSpec.val(revColAvp))
                        ),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        )
                )
        );
    }
}
