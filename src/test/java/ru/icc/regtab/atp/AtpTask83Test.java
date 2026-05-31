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
 * Task 83: implicit ATTR header subtable; repeating explicit subtables — first row
 * anchors (RT*, BW)->REC with inherited COL->AVP; second row produces 'D'-labelled
 * VALs followed by skip cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_83/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask83Test}
 */
class AtpTask83Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW    = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "83"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        colAvp,
                                        ActionSpec.rec(
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF),
                                                ProviderSpec.val(1, BELOW)
                                        )
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(colAvp))
                        ),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("D"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
