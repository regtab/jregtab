package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.FilterTerm;
import ru.icc.regtab.itm.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 45: flat table where each row has a non-blank anchor and a non-blank
 * delimited cell whose comma-separated values each reference same-subrow column 0.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_45/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask45Test}
 */
class AtpTask45Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW_COL0 = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(0));

    @Override
    protected String taskId() {
        return "45";
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
