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
 * Task 25: flat table where each row's first cell uses SUFFIX, slash-delimited REC
 * over values to the right, and JOIN(0) to group rows with the same ID string.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_025/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask025Test}
 */
class AtpTask025Test extends AtpTaskBase {

    private static final String SEP = "/";

    private static final ItemFilterConditionSpec RIGHT_OF          = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR         = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);
    private static final ItemFilterConditionSpec SUBROW_AFTER_ANCHOR = ItemFilterConditionSpec.and(FilterTerm.RightOf.INSTANCE, new FilterTerm.ColRange(2, Integer.MAX_VALUE));

    @Override
    protected String taskId() {
        return "025";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.suffix(SEP, ProviderSpec.any(1, RIGHT_OF)),
                                        ActionSpec.rec(SEP, ProviderSpec.val(ProviderSpec.UNBOUNDED, SUBROW_AFTER_ANCHOR)),
                                        ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                ));
    }
}
