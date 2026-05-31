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
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 72: header ATTR row then data rows with inherited COL->AVP — each data cell
 * is conditional: blank cells are skipped; non-blank cells carry RT*->REC or just AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_72/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask72Test}
 */
class AtpTask72Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "72"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.skip(),
                                        AtomicContentSpec.val(
                                                colAvp,
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                        )
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.skip(),
                                        AtomicContentSpec.val(colAvp)
                                ))
                        )
                )
        );
    }
}
