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
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 76: skip+header implicit subtable; repeating explicit subtables each with a
 * VAL+blanks first row and trimmed-VAL rows whose non-blank cells reference COL,
 * subtable-col-0, and ROW providers.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_76/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask76Test}
 */
class AtpTask76Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final StringExtractor TRIM = StringExtractor.Trimmed.INSTANCE;

    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec ST_COL0  = ItemFilterConditionSpec.and(
            FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(0));

    @Override
    protected String taskId() { return "76"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null)
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(TRIM)),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(
                                                ProviderSpec.val(SAME_COL),
                                                ProviderSpec.val(ST_COL0),
                                                ProviderSpec.val(SAME_ROW)
                                        )
                                ))
                        )
                )
        );
    }
}
