package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 100: cells contain literal-\n-separated values; the first cell anchors
 * three REC records via (RT&amp;P0)*-&gt;REC, (RT&amp;P1)*-&gt;REC, (RT&amp;P2)*-&gt;REC,
 * collecting values at the matching position from the two right-of cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_100/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask100Test}
 * <pre>
 * [ [VAL: (RT&amp;P0)*-&gt;REC '\n' VAL: (RT&amp;P1)*-&gt;REC '\n' VAL: (RT&amp;P2)*-&gt;REC]
 *   [VAL '\n' VAL '\n' VAL]{2} ]+
 * </pre>
 * The separator '\n' is the 2-char literal backslash+n used inside cells.
 */
class AtpTask100Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RT_P0 =
            ItemFilterConditionSpec.and(FilterTerm.RightOf.INSTANCE, new FilterTerm.PosExact(0));
    private static final ItemFilterConditionSpec RT_P1 =
            ItemFilterConditionSpec.and(FilterTerm.RightOf.INSTANCE, new FilterTerm.PosExact(1));
    private static final ItemFilterConditionSpec RT_P2 =
            ItemFilterConditionSpec.and(FilterTerm.RightOf.INSTANCE, new FilterTerm.PosExact(2));

    @Override
    protected String taskId() { return "100"; }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec anchorSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RT_P0))),
                CompoundContentSpec.Segment.of("\\n",
                        AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RT_P1)))),
                CompoundContentSpec.Segment.of("\\n",
                        AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RT_P2))))
        );

        CompoundContentSpec rightSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(),
                CompoundContentSpec.Segment.of("\\n", AtomicContentSpec.val()),
                CompoundContentSpec.Segment.of("\\n", AtomicContentSpec.val())
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(anchorSpec),
                                CellPattern.of(Quantifier.exactly(2), rightSpec)
                        )
                )
        );
    }
}
