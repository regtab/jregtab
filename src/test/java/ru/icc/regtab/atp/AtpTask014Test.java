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
 * Task 14: repeated subtables with a non-blank header row and data rows
 * combining two subtable-scoped column lookups with two same-row values via REC(4).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_014/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask014Test}
 */
class AtpTask014Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL0 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(0));
    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL1 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(1));
    private static final ItemFilterConditionSpec SAME_SUBROW        = ItemFilterConditionSpec.sameSubrow();

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() {
        return "014";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(4, ProviderSpec.val(1, SAME_SUBTABLE_COL0),
                                                       ProviderSpec.val(1, SAME_SUBTABLE_COL1),
                                                       ProviderSpec.val(2, SAME_SUBROW))
                                ))
                        )
                ));
    }
}
