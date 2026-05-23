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
 * Task 52: cross-table unpivot identical to task 51 but with a constant
 * YEAR=2025 attribute-value pair injected into every record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_52/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask52Test}
 */
class AtpTask52Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_COL  = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_ROW  = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() { return "52"; }

    @Override
    protected TablePattern buildPattern() {
        var dataCell = CompoundContentSpec.of(
                AtomicContentSpec.val(
                        ActionSpec.rec(
                                ProviderSpec.val(1, SAME_COL),
                                ProviderSpec.val(1, SAME_ROW),
                                ProviderSpec.val(1, SAME_CELL),
                                ProviderSpec.ctxAvp("YEAR", "2025")
                        ),
                        ActionSpec.avp("ND")
                ),
                CompoundContentSpec.Segment.of(" ",
                        AtomicContentSpec.val(ActionSpec.avp("MON"))
                )
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("AIRLINE")))
                        )
                ),
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
                                CellPattern.of(Quantifier.oneOrMore(), dataCell)
                        )
                )
        );
    }
}
