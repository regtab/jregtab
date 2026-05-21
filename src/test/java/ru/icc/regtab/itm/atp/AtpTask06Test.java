package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 06: repeated subtables — first row has a subtable-wide REC anchor
 * then conditional cells; four following rows contain only conditional cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_06/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask06Test}
 */
class AtpTask06Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();

    @Override
    protected String taskId() {
        return "06";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        new ConditionalContentSpec(BLANK, AtomicContentSpec.skip(), AtomicContentSpec.val()))
                        ),
                        RowPattern.of(Quantifier.exactly(4),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        new ConditionalContentSpec(BLANK, AtomicContentSpec.skip(), AtomicContentSpec.val()))
                        )
                )
        );
    }
}
