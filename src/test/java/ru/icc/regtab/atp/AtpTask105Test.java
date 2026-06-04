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

import java.util.List;

/**
 * Task 105: table-level cell match condition ('\d+') combined with NCL provider.
 * Every cell must contain only digits. The anchor (first cell of first row) collects
 * all other table values via NCL (not-same-cell) into a single flat record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_105/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask105Test}
 * <pre>
 * '\d+' ?
 * [ [VAL: NCL*-&gt;REC] [VAL]+ ]
 * [ [VAL]+ ]+
 * </pre>
 */
class AtpTask105Test extends AtpTaskBase {

    private static final CellMatchCondition TABLE_COND =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
    private static final ItemFilterConditionSpec NOT_SAME_CELL =
            new ItemFilterConditionSpec.Bare(FilterTerm.NotSameCell.INSTANCE);

    @Override
    protected String taskId() {
        return "105";
    }

    @Override
    protected TablePattern buildPattern() {
        SubtablePattern subtable = SubtablePattern.of(
                RowPattern.of(
                        CellPattern.of(AtomicContentSpec.val(
                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, NOT_SAME_CELL))
                        )),
                        CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                ),
                RowPattern.of(Quantifier.oneOrMore(),
                        CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                )
        );
        return new TablePattern(TABLE_COND, List.of(subtable), List.of());
    }
}
