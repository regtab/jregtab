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
 * Task 29: flat table where each physical row is split into a 6-cell header subrow
 * and one-or-more 4-cell data subrows with a composite REC anchor.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_029/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask029Test}
 */
class AtpTask029Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() {
        return "029";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(Quantifier.exactly(6), AtomicContentSpec.val())
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.rec(6, ProviderSpec.val(6, SAME_ROW), ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                        )),
                                        CellPattern.of(Quantifier.exactly(3), AtomicContentSpec.val())
                                )
                        )
                ));
    }
}
