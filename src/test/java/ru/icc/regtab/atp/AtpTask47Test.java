package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 47: repeated subtables with one-or-more non-blank two-cell rows —
 * anchor VAL with same-row REC and below-same-string JOIN(0), plus a plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_47/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask47Test}
 */
class AtpTask47Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() {
        return "47";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBROW)),
                                        ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
