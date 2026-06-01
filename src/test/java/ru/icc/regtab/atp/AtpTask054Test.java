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
 * Task 54: flat table with repeating subrow groups — header subrows and data subrows
 * where anchor VAL collects same-subcol and same-subrow items into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_054/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask054Test}
 */
class AtpTask054Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBCOL = ItemFilterConditionSpec.sameSubcol();
    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() { return "054"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.skip(),
                                        CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val()),
                                        CellPattern.of(BLANK, Quantifier.zeroOrOne(), null)
                                )
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val()),
                                        CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                                ActionSpec.rec(
                                                        ProviderSpec.val(SAME_SUBCOL),
                                                        ProviderSpec.val(SAME_SUBROW)
                                                )
                                        )),
                                        CellPattern.of(BLANK, Quantifier.zeroOrOne(), null)
                                )
                        )
                )
        );
    }
}
