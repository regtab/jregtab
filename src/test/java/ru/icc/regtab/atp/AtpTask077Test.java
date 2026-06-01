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


/**
 * Task 77: exactly two header VAL rows with REC pointing two rows below; exactly
 * two data VAL rows without actions.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_077/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask077Test}
 */
class AtpTask077Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BW_R2 = ItemFilterConditionSpec.and(
            FilterTerm.Below.INSTANCE, new FilterTerm.RowOffset(2));

    @Override
    protected String taskId() { return "077"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(BW_R2))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
