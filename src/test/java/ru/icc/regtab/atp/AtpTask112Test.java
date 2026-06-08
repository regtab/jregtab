package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 112: cross-tabulation with location header, compound INDICATOR/UNIT cell,
 * and MIN/MAX/AVE subrows with blank AVE handling.
 * REC on AVE: ROW{3} (INDICATOR, UNIT, YEAR), -LT{2} (MAX, MIN), COL (LOCATION).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_112/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask112Test}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'->AVP]+ ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
 *   [VAL: 'YEAR'->AVP] { [VAL: 'MIN'->AVP] [VAL: 'MAX'->AVP]
 *   [BLANK ? _ | VAL: 'AVE'->AVP, (ROW{3},-LT{2},COL)->REC] }+ ]+
 * </pre>
 */
class AtpTask112Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "112"; }

    @Override
    protected TablePattern buildPattern() {
        ContentSpec indicatorUnit = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("INDICATOR")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("UNIT"))));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(3, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.leftOf()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        ContentSpec aveSpec = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("LOCATION")))
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.of(indicatorUnit),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MIN"))),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                                        CellPattern.of(aveSpec)
                                )
                        )
                )
        );
    }
}
