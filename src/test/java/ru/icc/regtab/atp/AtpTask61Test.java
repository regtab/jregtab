package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 61: each row has one-or-more compound cells — anchor VAL collects same-cell
 * values into REC with literal 'A' AVP, followed by two space-delimited VAL segments
 * labelled 'B' and 'N'.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_61/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask61Test}
 */
class AtpTask61Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "61"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), CompoundContentSpec.of(
                                        AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL)),
                                                ActionSpec.avp("A")
                                        ),
                                        CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.avp("B"))),
                                        CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.avp("N")))
                                ))
                        )
                )
        );
    }
}
