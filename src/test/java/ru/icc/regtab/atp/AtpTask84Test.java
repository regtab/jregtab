package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 84: repeating rows with a leading plain VAL and one-or-more explicit subrows each
 * containing a VAL anchor (ROW, RT)->REC(1) and a plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_84/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask84Test}
 */
class AtpTask84Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "84"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.of(AtomicContentSpec.val())
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.rec(1,
                                                        ProviderSpec.val(SAME_ROW),
                                                        ProviderSpec.val(RIGHT_OF))
                                        )),
                                        CellPattern.of(AtomicContentSpec.val())
                                )
                        )
                )
        );
    }
}
