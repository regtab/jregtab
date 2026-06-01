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
 * Task 03: flat table with a row-key anchor followed by exactly two value
 * cells per row, each referencing the anchor via same-subrow REC(1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_003/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask003Test}
 */
class AtpTask003Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() {
        return "003";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val(
                                        ActionSpec.rec(1, ProviderSpec.val(1, SAME_SUBROW))
                                ))
                        )
                ));
    }
}
