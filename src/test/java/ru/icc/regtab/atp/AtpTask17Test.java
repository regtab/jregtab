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
 * Task 17: repeated subtables where the header cell collects all values below
 * via unbounded REC, followed by non-blank data rows and an optional skip row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_17/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask17Test}
 */
class AtpTask17Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() {
        return "17";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.skip()
                        )
                )
        );
    }
}
