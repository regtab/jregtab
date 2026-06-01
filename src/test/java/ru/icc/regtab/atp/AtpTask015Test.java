package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 15: flat table where each cell contains a compound value — three
 * space-separated parts each collected via same-cell REC(1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_015/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask015Test}
 */
class AtpTask015Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() {
        return "015";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec compoundSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(1, ProviderSpec.val(1, SAME_CELL)))),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(1, ProviderSpec.val(1, SAME_CELL)))),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(1, ProviderSpec.val(1, SAME_CELL))))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(compoundSpec)
                        )
                ));
    }
}
