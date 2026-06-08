package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 123: compound YEAR\nMONTH header and compound MIN-MAX\nAVE data cells.
 * REC on AVE: CL* (MIN, MAX, YEAR, MONTH), ROW (INDICATOR), COL&R1* (YEAR, MONTH in header).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_123/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask123Test}
 * <pre>
 * [ []+ ]
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP '\n' VAL: 'MONTH'->AVP]{4} [] ]
 * [ [VAL: 'INDICATOR'->AVP]
 *   [VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\n' VAL: 'AVE'->AVP, (CL*,ROW,COL&R1*)->REC]{4} [] ]+
 * </pre>
 */
class AtpTask123Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "123"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ContentSpec yearMonth = CompoundContentSpec.of(
                AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR")),
                CompoundContentSpec.Segment.of("\\n",
                        AtomicContentSpec.val(ActionSpec.avp("MONTH"))));

        ItemFilterConditionSpec colR1 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(ProviderSpec.UNBOUNDED, colR1));

        ContentSpec minMaxAve = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("MIN")),
                CompoundContentSpec.Segment.of("-", AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                CompoundContentSpec.Segment.of("\\n",
                        AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve)));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(4), yearMonth),
                                CellPattern.skip()
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(Quantifier.exactly(4), minMaxAve),
                                CellPattern.skip()
                        )
                )
        );
    }
}
