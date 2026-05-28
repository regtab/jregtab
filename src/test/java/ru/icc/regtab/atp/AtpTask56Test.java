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


/**
 * Task 56: two-row-type table — first ATTR row is the anchor for same-col
 * transpose via BW* REC; subsequent ATTR rows provide same-row AVP labels.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_56/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask56Test}
 */
class AtpTask56Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW    = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "56"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW)),
                                        ActionSpec.avp(ProviderSpec.attr(SAME_ROW))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_ROW))
                                ))
                        )
                )
        );
    }
}
