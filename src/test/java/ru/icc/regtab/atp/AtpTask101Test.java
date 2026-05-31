package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 101: each cell contains tab-separated values; the first value anchors
 * a REC record via CL*-&gt;REC, collecting the remaining values supplied by the
 * tab-delimited (VAL){'\t'} segment.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_101/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask101Test}
 * <pre>
 * [ [VAL: CL*-&gt;REC '\t' (VAL){'\t'}]+ ]+
 * </pre>
 */
class AtpTask101Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "101"; }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec cellSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL))),
                CompoundContentSpec.Segment.of("\t",
                        new DelimitedContentSpec("\t", AtomicContentSpec.val()))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), cellSpec)
                        )
                )
        );
    }
}
