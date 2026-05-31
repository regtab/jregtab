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
 * Task 78: single explicit subtable with inherited ROW->AVP — first row anchors
 * BW*->REC; subsequent rows carry only the inherited AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_78/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask78Test}
 */
class AtpTask78Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW    = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "78"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec rowAvp = ActionSpec.avp(ProviderSpec.attr(SAME_ROW));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        rowAvp,
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(rowAvp))
                        )
                )
        );
    }
}
