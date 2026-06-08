package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 128: cross-tabulation with LOCATION header and compound TIME YEAR cell.
 * Dash-only cells skipped. REC on AVE: CL* (MIN, MAX), ROW&C1* (TIME, YEAR), COL (LOCATION).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_128/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask128Test}
 * <pre>
 * [ []{2} [VAL: 'LOCATION'->AVP]+ ]
 * [ [VAL: 'HYDROBIONT_GROUP'->AVP] [VAL: 'TIME'->AVP ' ' VAL: 'YEAR'->AVP]
 *   ['\s*-?\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\n'
 *                     VAL: 'AVE'->AVP, (CL*,ROW&C1*,COL)->REC]+ ]+
 * </pre>
 */
class AtpTask128Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "128"; }

    @Override
    protected TablePattern buildPattern() {
        ContentSpec timeYear = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("TIME")),
                CompoundContentSpec.Segment.of(" ",
                        AtomicContentSpec.val(ActionSpec.avp("YEAR"))));

        ItemFilterConditionSpec rowC1 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(1));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(ProviderSpec.UNBOUNDED, rowC1),
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
                                        AtomicContentSpec.val(ActionSpec.avp("LOCATION")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("HYDROBIONT_GROUP"))),
                                CellPattern.of(timeYear),
                                CellPattern.of(Quantifier.oneOrMore(), cellSpec)
                        )
                )
        );
    }
}
