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
 * Task 71: header rows accumulate hierarchical path via SUFFIX('/') on #H-tagged
 * cells; row headers accumulate via SUFFIX('/') on #S-tagged cells; data VALs
 * collect COL and ROW providers into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_71/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask71Test}
 */
class AtpTask71Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_DIGIT = new CellMatchCondition(new CellPredicate.NotRegexMatched("\\d+"));
    private static final CellMatchCondition DIGIT     = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));

    private static final ItemFilterConditionSpec BW_H = ItemFilterConditionSpec.and(
            FilterTerm.Below.INSTANCE, new FilterTerm.Tagged("#H"));
    private static final ItemFilterConditionSpec RT_S = ItemFilterConditionSpec.and(
            FilterTerm.RightOf.INSTANCE, new FilterTerm.Tagged("#S"));
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "71"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#H",
                                        ActionSpec.suffix("/", ProviderSpec.any(ProviderSpec.UNBOUNDED, BW_H))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#S",
                                        ActionSpec.suffix("/", ProviderSpec.any(ProviderSpec.UNBOUNDED, RT_S))
                                )),
                                CellPattern.of(DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(SAME_COL), ProviderSpec.val(SAME_ROW))
                                ))
                        )
                )
        );
    }
}
