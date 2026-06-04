package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 11: cross-table unpivot with a column-header row and data rows using
 * explicit subrow grouping and conditional blank-skipping.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_011/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask011Test}
 */
class AtpTask011Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW    = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec SAME_SUBCOLUMN = ItemFilterConditionSpec.sameSubcol();

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() {
        return "011";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.val()),
                                        CellPattern.of(Quantifier.oneOrMore(),
                                new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.skip(),
                                        AtomicContentSpec.val(ActionSpec.rec(2, ProviderSpec.val(1, SAME_SUBROW), ProviderSpec.val(1, SAME_SUBCOLUMN)))))
                                )
                        )
                ));
    }
}
