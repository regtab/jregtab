package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 131: MPC exceedance frequency table with YEAR header.
 * Cells containing '*' are skipped. REC: ROW{2} (POLLUTANT, MPC), ROW&C4, COL&R1 (YEAR).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_131/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask131Test}
 * <pre>
 * [ []+ ]
 * [ []{2} [VAL=SUBSTR(0,4): 'YEAR'->AVP]{2} [] ]
 * [ [VAL: 'POLLUTANT'->AVP] [VAL: 'MPC'->AVP]
 *   [~'*' ? _ | VAL: 'MPC_EXCEEDING_FREQUENCY'->AVP, (ROW{2},ROW&C4,COL&R1)->REC]{2}
 *   [] ]+
 * </pre>
 */
class AtpTask131Test extends AtpTaskBase {

    private static final CellMatchCondition CONTAINS_STAR =
            new CellMatchCondition(new CellPredicate.Contains("*"));

    @Override
    protected String taskId() { return "131"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ItemFilterConditionSpec rowC4 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(4));
        ItemFilterConditionSpec colR1 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));

        ActionSpec recFreq = ActionSpec.rec(
                ProviderSpec.val(2, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, rowC4),
                ProviderSpec.val(1, colR1));

        ContentSpec freqSpec = new ConditionalContentSpec(CONTAINS_STAR,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("MPC_EXCEEDING_FREQUENCY"), recFreq));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.exactly(2),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR"))),
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("POLLUTANT"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("MPC"))),
                                CellPattern.of(Quantifier.exactly(2), freqSpec),
                                CellPattern.skip()
                        )
                )
        );
    }
}
