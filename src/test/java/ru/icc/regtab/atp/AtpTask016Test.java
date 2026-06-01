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
 * Task 16: flat table where each anchor cell collects one value to the right
 * via REC and joins same-string cells below via JOIN(0).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_016/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask016Test}
 */
class AtpTask016Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() {
        return "016";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(1, RIGHT_OF)),
                                        ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
