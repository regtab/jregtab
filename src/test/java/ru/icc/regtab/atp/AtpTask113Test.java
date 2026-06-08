package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 113: emission table with YEAR header (SUBSTR 0..4) and POLLUTANT data rows.
 * REC on EMISSION collects same-row POLLUTANT (ROW, cardinality 1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_113/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask113Test}
 * <pre>
 * [ []+ ]
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{7} []* ]
 * [ [VAL: 'POLLUTANT'->AVP] [VAL: 'EMISSION'->AVP, ROW->REC]{7} []* ]+
 * </pre>
 */
class AtpTask113Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "113"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ActionSpec recRow = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(7),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR"))),
                                CellPattern.skip(Quantifier.zeroOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("POLLUTANT"))),
                                CellPattern.of(Quantifier.exactly(7),
                                        AtomicContentSpec.val(ActionSpec.avp("EMISSION"), recRow)),
                                CellPattern.skip(Quantifier.zeroOrMore())
                        )
                )
        );
    }
}
