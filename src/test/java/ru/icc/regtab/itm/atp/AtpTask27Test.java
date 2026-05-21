package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 27: repeated subtables with a single-cell header collecting all values
 * below, one skip-row separator, and exactly 9 plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_27/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask27Test}
 */
class AtpTask27Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() {
        return "27";
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
                        RowPattern.of(
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.exactly(9),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
