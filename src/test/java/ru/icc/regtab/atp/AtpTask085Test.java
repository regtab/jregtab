package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import static ru.icc.regtab.atp.spec.ItemFilterConditionSpec.and;


/**
 * Task 85: fixed 3×3 grid with three anchors at absolute row/column positions.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_085/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask085Test}
 */
class AtpTask085Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec R2_C1 =
            and(new FilterTerm.RowExact(2), new FilterTerm.ColExact(1));
    private static final ItemFilterConditionSpec R0_C2 =
            and(new FilterTerm.RowExact(0), new FilterTerm.ColExact(2));
    private static final ItemFilterConditionSpec R0_C1 =
            and(new FilterTerm.RowExact(0), new FilterTerm.ColExact(1));

    @Override
    protected String taskId() { return "085"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(R2_C1))
                                )),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(R0_C2))
                                )),
                                CellPattern.skip()
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(R0_C1))
                                ))
                        )
                )
        );
    }
}
