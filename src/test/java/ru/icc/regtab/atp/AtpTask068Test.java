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
 * Task 68: tagged header rows (#HEAD) followed by data rows — anchor VAL uses
 * REC collecting same-col #HEAD items and same-row items.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_068/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask068Test}
 */
class AtpTask068Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec COL_HEAD = ItemFilterConditionSpec.and(
            FilterTerm.SameCol.INSTANCE, new FilterTerm.Tagged("#HEAD"));
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "068"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#HEAD"))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, COL_HEAD),
                                                ProviderSpec.val(SAME_ROW)
                                        )
                                ))
                        )
                )
        );
    }
}
