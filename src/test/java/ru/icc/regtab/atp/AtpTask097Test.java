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

import java.util.Set;

/**
 * Task 97: flat table where each row's anchor VAL collects same-subrow values to the right
 * via RT*->REC, and joins all records of same-string VALs below in the same subcol
 * via (BW&STR)*->JOIN(0,1), dropping key positions {0,1} from each joined record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_097/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask097Test}
 */
class AtpTask097Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF    = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR   =
            ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() { return "097"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF)),
                                        ActionSpec.join(Set.of(0, 1), ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
