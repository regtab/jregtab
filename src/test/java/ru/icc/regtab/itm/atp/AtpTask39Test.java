package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 39: flat table where each cell is a compound value — a price part collected
 * via same-cell REC, a bedroom count, and a trailing skip part.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_39/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask39Test}
 */
class AtpTask39Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() {
        return "39";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec priceBedroomSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL))),
                CompoundContentSpec.Segment.of(" / ", AtomicContentSpec.val()),
                CompoundContentSpec.Segment.of("br", AtomicContentSpec.skip())
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(priceBedroomSpec)
                        )
                )
        );
    }
}
