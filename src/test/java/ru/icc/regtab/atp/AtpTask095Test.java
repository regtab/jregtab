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
 * Task 95: repeated subtables where each cell is a compound ATTR=VAL; the first
 * row collects same-subcol values below via BW*->REC, and CL->AVP (inherited at
 * subtable level) resolves the attribute from the same cell for every row.
 * Exactly 2 data rows follow the header row in each subtable.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_095/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask095Test}
 */
class AtpTask095Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW     = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "095"; }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec headerCell = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW)).asInherited(),
                        ActionSpec.avp(ProviderSpec.attr(SAME_CELL)).asInherited()
                ))
        );

        CompoundContentSpec dataCell = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.avp(ProviderSpec.attr(SAME_CELL)).asInherited()
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), headerCell)
                        ),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(Quantifier.oneOrMore(), dataCell)
                        )
                )
        );
    }
}
