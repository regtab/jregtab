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
 * Task 43: flat table where each row has a name anchor cell followed by exactly
 * three compound ATTR:VAL cells, each with a same-cell AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_43/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask43Test}
 */
class AtpTask43Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() {
        return "43";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec subjectValue = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of(":", AtomicContentSpec.val(
                        ActionSpec.avp(ProviderSpec.attr(SAME_CELL))
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.exactly(3), subjectValue)
                        )
                )
        );
    }
}
