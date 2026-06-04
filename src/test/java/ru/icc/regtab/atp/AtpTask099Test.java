package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 99: each cell is a compound "KEY=VAL\r\nKEY=VAL\r\nKEY=VAL" value —
 * ATTRs at positions 0, 2, 4 name the VALs via AVP; VAL at position 1 anchors
 * the record via CL*->REC (collecting sibling VALs at positions 3 and 5).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_099/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask099Test}
 */
class AtpTask099Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec CL_P0 =
            ItemFilterConditionSpec.and(FilterTerm.SameCell.INSTANCE, new FilterTerm.PosExact(0));
    private static final ItemFilterConditionSpec CL_P2 =
            ItemFilterConditionSpec.and(FilterTerm.SameCell.INSTANCE, new FilterTerm.PosExact(2));
    private static final ItemFilterConditionSpec CL_P4 =
            ItemFilterConditionSpec.and(FilterTerm.SameCell.INSTANCE, new FilterTerm.PosExact(4));
    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "099"; }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec cellSpec = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=",
                        AtomicContentSpec.val(
                                ActionSpec.avp(ProviderSpec.attr(CL_P0)),
                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL))
                        )),
                CompoundContentSpec.Segment.of("\r\n", AtomicContentSpec.attr()),
                CompoundContentSpec.Segment.of("=",
                        AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.attr(CL_P2)))),
                CompoundContentSpec.Segment.of("\r\n", AtomicContentSpec.attr()),
                CompoundContentSpec.Segment.of("=",
                        AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.attr(CL_P4))))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), cellSpec)
                        )
                )
        );
    }
}
