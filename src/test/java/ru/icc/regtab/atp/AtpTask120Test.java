package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 120: cross-tabulation with LOCATION header, compound INDICATOR/UNIT cell,
 * and fully populated AVE cells (no blank handling).
 * REC on AVE: ROW{2} (INDICATOR, UNIT), -LT{2} (MAX, MIN), COL (LOCATION).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_120/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask120Test}
 * <pre>
 * [ [] [VAL: 'LOCATION'->AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
 *   { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
 *     [VAL: 'AVE'->AVP, (ROW{2},-LT{2},COL)->REC] }+ ]+
 * </pre>
 */
class AtpTask120Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "120"; }

    @Override
    protected TablePattern buildPattern() {
        ContentSpec indUnit = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("INDICATOR")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("UNIT"))));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(2, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.leftOf()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("LOCATION")))
                        ),
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.of(indUnit)
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MIN"))),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve))
                                )
                        )
                )
        );
    }
}
