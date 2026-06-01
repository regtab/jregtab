package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 45: flat table where each row has a non-blank anchor and a non-blank
 * delimited cell whose comma-separated values each reference same-subrow column 0.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_045/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask045Test}
 */
class AtpTask045Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW_COL0 = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(0));

    @Override
    protected String taskId() {
        return "045";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(),
                                        new DelimitedContentSpec(",", AtomicContentSpec.val(
                                                ActionSpec.rec(1, ProviderSpec.val(1, SAME_SUBROW_COL0))
                                        ))
                                )
                        )
                ));
    }
}
