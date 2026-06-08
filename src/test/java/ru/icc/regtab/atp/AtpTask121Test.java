package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 121: monthly cross-tabulation with compound MIN-MAX\nAVE cells.
 * Dash-only cells are skipped. REC on AVE: CL* (MIN, MAX), ROW&C1 (INDICATOR), COL (MONTH).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_121/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask121Test}
 * <pre>
 * [ []{2} [VAL: 'MONTH'->AVP]+ ]
 * [ [] [VAL: 'INDICATOR'->AVP]
 *   ['\s*-?\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\n'
 *                     VAL: 'AVE'->AVP, (CL*,ROW&C1,COL)->REC]+ ]+
 * </pre>
 */
class AtpTask121Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "121"; }

    @Override
    protected TablePattern buildPattern() {
        ItemFilterConditionSpec rowC1 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(1));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(1, rowC1),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        ContentSpec minMaxAve = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("MIN")),
                CompoundContentSpec.Segment.of("-", AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                CompoundContentSpec.Segment.of("\\n",
                        AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve)));

        ContentSpec cellSpec = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(), minMaxAve);

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
                                CellPattern.of(Quantifier.oneOrMore(), cellSpec)
                        )
                )
        );
    }
}
