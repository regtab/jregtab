package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 125: simple AVE-only cross-tabulation with MONTH header.
 * Dash-only cells skipped. REC on AVE: ROW&C1 (INDICATOR), COL (MONTH).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_125/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask125Test}
 * <pre>
 * [ []{2} [VAL: 'MONTH'->AVP]+ ]
 * [ [] [VAL: 'INDICATOR'->AVP]
 *   ['\s*-?\s*' ? _ | VAL: 'AVE'->AVP, (ROW&C1,COL)->REC]+ ]+
 * </pre>
 */
class AtpTask125Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "125"; }

    @Override
    protected TablePattern buildPattern() {
        ItemFilterConditionSpec rowC1 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(1));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(1, rowC1),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        ContentSpec aveSpec = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("MONTH")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(Quantifier.oneOrMore(), aveSpec)
                        )
                )
        );
    }
}
