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
 * Task 70: header rows with blank guards and #H-tagged VALs; data rows with
 * non-digit #S-tagged anchors and digit-guarded VALs collecting (COL & #H)* and
 * (ROW & #S)* into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_070/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask070Test}
 */
class AtpTask070Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK      = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_DIGIT  = new CellMatchCondition(new CellPredicate.NotRegexMatched("\\d+"));
    private static final CellMatchCondition DIGIT      = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));

    private static final ItemFilterConditionSpec COL_H = ItemFilterConditionSpec.and(
            FilterTerm.SameCol.INSTANCE, new FilterTerm.Tagged("#H"));
    private static final ItemFilterConditionSpec ROW_S = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#S"));

    @Override
    protected String taskId() { return "070"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#H"))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#S")),
                                CellPattern.of(DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, COL_H),
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_S)
                                        )
                                ))
                        )
                )
        );
    }
}
