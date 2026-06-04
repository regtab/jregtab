package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.CompoundSegment;
import ru.icc.regtab.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import java.util.List;


/**
 * Task 55: each cell holds a comma-separated list — anchor VAL (same-cell REC)
 * followed by a comma-delimited VAL sequence.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_055/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask055Test}
 */
class AtpTask055Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "055"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(new CompoundContentSpec(List.of(
                                        new CompoundSegment("", AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL))
                                        )),
                                        new CompoundSegment(",", new DelimitedContentSpec(",", AtomicContentSpec.val()))
                                ), ""))
                        )
                )
        );
    }
}
