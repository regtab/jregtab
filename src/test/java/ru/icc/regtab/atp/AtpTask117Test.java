package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 117: discharge table with two EMISSION row groups (MLN M3 and TONS).
 * REC collects POLLUTANT from col 1 in same row (ROW&C1), YEAR from col 1 at row 1 (COL&R1),
 * and a constant UNIT literal.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_117/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask117Test}
 * <pre>
 * [ []+ ]
 * [ [] [ATTR] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{5} [] ]
 * [ [] [VAL: 'POLLUTANT'->AVP]
 *   [VAL: 'EMISSION'->AVP, (ROW&C1,COL&R1,@'UNIT'='MLN M3')->REC]{5} [] ]
 * [ [] [VAL: 'POLLUTANT'->AVP]
 *   [VAL: 'EMISSION'->AVP, (ROW&C1,COL&R1,@'UNIT'='TONS')->REC]{5} [] ]+
 * </pre>
 */
class AtpTask117Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "117"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ItemFilterConditionSpec rowC1 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(1));
        ItemFilterConditionSpec colR1 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));

        ActionSpec recMln = ActionSpec.rec(
                ProviderSpec.val(1, rowC1),
                ProviderSpec.val(1, colR1),
                ProviderSpec.ctxAvp("UNIT", "MLN M3"));

        ActionSpec recTons = ActionSpec.rec(
                ProviderSpec.val(1, rowC1),
                ProviderSpec.val(1, colR1),
                ProviderSpec.ctxAvp("UNIT", "TONS"));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR"))),
                                CellPattern.skip()
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("POLLUTANT"))),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(ActionSpec.avp("EMISSION"), recMln)),
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("POLLUTANT"))),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(ActionSpec.avp("EMISSION"), recTons)),
                                CellPattern.skip()
                        )
                )
        );
    }
}
