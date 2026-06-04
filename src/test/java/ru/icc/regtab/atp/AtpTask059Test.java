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
 * Task 59: three-cell rows — anchor VAL (right-of REC), a VAL that appends suffix
 * from all cells to the right, followed by one-or-more AUX cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_059/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask059Test}
 */
class AtpTask059Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "059"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(RIGHT_OF))
                                )),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.suffix(", ", ProviderSpec.any(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())
                        )
                )
        );
    }
}
