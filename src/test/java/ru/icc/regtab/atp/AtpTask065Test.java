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
 * Task 65: two-cell rows — anchor VAL appends right-of suffix (', ') and collects
 * RT->REC; adjacent compound cell starts with AUX followed by a comma-delimited VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_065/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask065Test}
 */
class AtpTask065Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "065"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.suffix(", ", ProviderSpec.any(1, RIGHT_OF)),
                                        ActionSpec.rec(ProviderSpec.val(1, RIGHT_OF))
                                )),
                                CellPattern.of(CompoundContentSpec.of(
                                        AtomicContentSpec.aux(),
                                        CompoundContentSpec.Segment.of(", ", AtomicContentSpec.val())
                                ))
                        )
                )
        );
    }
}
