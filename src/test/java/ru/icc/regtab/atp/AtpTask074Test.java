package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 74: header row with exactly 3 ATTR cells; data rows with inherited COL->AVP
 * — anchor VAL collects RT* and a constant @'D'='d' provider into REC; two more VALs.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_074/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask074Test}
 */
class AtpTask074Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "074"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(3), AtomicContentSpec.attr())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        colAvp,
                                        ActionSpec.rec(
                                                ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF),
                                                ProviderSpec.ctxAvp("D", "d")
                                        )
                                )),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
