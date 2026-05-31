package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 62: repeating subtables — skip+header row, data rows guarded by !"x"? with
 * REC gathering above and left-of items, and optional trailer rows matching 'x'.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_62/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask62Test}
 */
class AtpTask62Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_X   = new CellMatchCondition(new CellPredicate.NotRegexMatched("x"));
    private static final CellMatchCondition MATCH_X = new CellMatchCondition(new CellPredicate.RegexMatched("x"));

    private static final ItemFilterConditionSpec ABOVE   = ItemFilterConditionSpec.above();
    private static final ItemFilterConditionSpec LEFT_OF = ItemFilterConditionSpec.leftOf();

    @Override
    protected String taskId() { return "62"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(NOT_X, Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ABOVE), ProviderSpec.val(LEFT_OF))
                                ))
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(MATCH_X, Quantifier.oneOrMore(), null)
                        )
                )
        );
    }
}
