package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 122: cross-tabulation with YEAR/MONTH headers and explicit subtable pairing
 * MIN-MAX row with AVE row. REC on AVE: ROW (INDICATOR), -AV{2} (MIN, MAX), COL{2} (YEAR, MONTH).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_122/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask122Test}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]+ ]
 * [ [] [VAL: 'MONTH'->AVP]+ ]
 * { [ [] ['\s*-?\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP]{6} [] ]
 *   [ [VAL: 'INDICATOR'->AVP]
 *     ['\s*-?\s*' ? _ | VAL: 'AVE'->AVP, (ROW,-AV{2},COL{2})->REC]{6} [] ] }+
 * </pre>
 */
class AtpTask122Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "122"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ContentSpec minMax = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                CompoundContentSpec.of(
                        AtomicContentSpec.val(ActionSpec.avp("MIN")),
                        CompoundContentSpec.Segment.of("-",
                                AtomicContentSpec.val(ActionSpec.avp("MAX")))));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, ItemFilterConditionSpec.above()),
                ProviderSpec.val(2, ItemFilterConditionSpec.sameCol()));

        ContentSpec aveSpec = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("MONTH")))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(6), minMax),
                                CellPattern.skip()
                        ),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(Quantifier.exactly(6), aveSpec),
                                CellPattern.skip()
                        )
                )
        );
    }
}
