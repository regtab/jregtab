package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 111: cross-tabulation with a unit header row, a subheader row, and data rows.
 * REC on AVE collects all same-row items (ROW*) and the same-column UNIT (COL).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_111/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask111Test}
 * <pre>
 * [ []{2} [VAL: 'UNIT'->AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'->AVP] [VAL: 'YEAR'->AVP] [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
 *   [VAL: 'AVE'->AVP, (ROW*,COL)->REC] ]+
 * </pre>
 */
class AtpTask111Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "111"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recRowCol = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("UNIT")))
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("YEAR"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MIN"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AVE"), recRowCol))
                        )
                )
        );
    }
}
