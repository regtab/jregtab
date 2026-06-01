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
 * Task 18: repeated subtables with one compound ATTR=VAL header row (collecting
 * subtable-wide values and AVP) and exactly 15 data rows with ATTR=VAL AVP only.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_018/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask018Test}
 */
class AtpTask018Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL      = ItemFilterConditionSpec.sameCell();
    private static final ItemFilterConditionSpec BELOW_SUBTABLE = ItemFilterConditionSpec.sameSubtable();

    @Override
    protected String taskId() {
        return "018";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec firstRow = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_SUBTABLE)),
                        ActionSpec.avp(ProviderSpec.attr(SAME_CELL))
                ))
        );

        CompoundContentSpec otherRows = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.avp(ProviderSpec.attr(SAME_CELL))
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(firstRow)
                        ),
                        RowPattern.of(Quantifier.exactly(15),
                                CellPattern.of(otherRows)
                        )
                )
        );
    }
}
