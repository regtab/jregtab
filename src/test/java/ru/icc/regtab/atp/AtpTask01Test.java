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
 * Task 01: two-row subtables — anchor VAL collects all same-subtable values
 * via REC (unbounded), plus a plain-value second row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_01/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask01Test}
 */
class AtpTask01Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();

    @Override
    protected String taskId() {
        return "01";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE))
                                )),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val()),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
