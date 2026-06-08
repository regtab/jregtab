package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 129: cross-tabulation with YEAR header and compound HYDROBIONT_GROUP/UNIT cell.
 * Mandatory-dash cells skipped. REC on AVE: CL* (MIN, MAX), ROW&C1* (HYDROBIONT_GROUP, UNIT),
 * COL (YEAR).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_129/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask129Test}
 * <pre>
 * [ [] [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]+ ]
 * [ [] [VAL: 'HYDROBIONT_GROUP'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
 *   ['\s*-\s*' ? _ | VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\n'
 *                    VAL: 'AVE'->AVP, (CL*,ROW&C1*,COL)->REC]+ ]+
 * </pre>
 */
class AtpTask129Test extends AtpTaskBase {

    private static final CellMatchCondition DASH =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-\\s*"));

    @Override
    protected String taskId() { return "129"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ContentSpec hgUnit = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("HYDROBIONT_GROUP")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("UNIT"))));

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

        ContentSpec cellSpec = new ConditionalContentSpec(DASH,
                AtomicContentSpec.skip(), minMaxAve);

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.skip(),
                                CellPattern.of(hgUnit),
                                CellPattern.of(Quantifier.oneOrMore(), cellSpec)
                        )
                )
        );
    }
}
