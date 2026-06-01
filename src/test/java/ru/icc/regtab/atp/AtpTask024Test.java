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
 * Task 24: flat single-column table with a header cell collecting all values
 * below via unbounded REC and one-or-more plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_024/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask024Test}
 */
class AtpTask024Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() {
        return "024";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
