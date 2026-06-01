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
 * Task 30: repeated subtables with a single-cell header collecting all values
 * below via unbounded REC, followed by exactly 3 plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_030/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask030Test}
 */
class AtpTask030Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() {
        return "030";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(3),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
