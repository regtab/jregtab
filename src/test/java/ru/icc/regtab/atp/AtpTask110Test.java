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
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 110: rows with non-blank anchor cells collecting right-of values via RT*->REC,
 * each group separated by an optional blank cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_110/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask110Test}
 * <pre>
 * [ { [!BLANK ? VAL: RT*-&gt;REC] [!BLANK ? VAL]+ [BLANK]? }+ ]+
 * </pre>
 */
class AtpTask110Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "110"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recRt = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.rightOf()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(recRt)),
                                        CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val()),
                                        CellPattern.of(BLANK, Quantifier.zeroOrOne(), null)
                                )
                        )
                )
        );
    }
}
