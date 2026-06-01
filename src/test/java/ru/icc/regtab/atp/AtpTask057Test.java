package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.CompoundSegment;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import java.util.List;


/**
 * Task 57: two-cell rows — anchor VAL with right-of REC, adjacent compound cell
 * splits on dash into two trimmed VAL segments.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_057/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask057Test}
 */
class AtpTask057Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final StringExtractor         TRIM     = StringExtractor.Trimmed.INSTANCE;

    @Override
    protected String taskId() { return "057"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(RIGHT_OF))
                                )),
                                CellPattern.of(new CompoundContentSpec(List.of(
                                        new CompoundSegment("", AtomicContentSpec.val(TRIM)),
                                        new CompoundSegment("-", AtomicContentSpec.val(TRIM))
                                ), ""))
                        )
                )
        );
    }
}
