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
 * Task 82: repeating subtables with inherited SC->AVP — each subtable has one
 * ATTR header row, one-or-more data rows (non-blank anchor with RT*->REC), and an
 * optional blank-guard trailer row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_82/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask82Test}
 */
class AtpTask82Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_SC  = ItemFilterConditionSpec.sameSubcol();

    @Override
    protected String taskId() { return "82"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec scAvp = ActionSpec.avp(ProviderSpec.attr(SAME_SC));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        scAvp,
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(scAvp))
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null)
                        )
                )
        );
    }
}
