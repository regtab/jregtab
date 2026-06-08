package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

import java.util.List;

/**
 * Task 124: separate YEAR/MONTH header rows and compound MIN-MAX\nAVE(IN_NW) data cells.
 * REC on AVE: CL* (MIN, MAX), ROW (INDICATOR), COL{2} (YEAR, MONTH), trailing ')'.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_124/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask124Test}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{6} []{2} ]
 * [ [] [VAL: 'MONTH'->AVP]{6} []{2} ]
 * [ [VAL: 'INDICATOR'->AVP]
 *   [VAL: 'MIN'->AVP '-' VAL: 'MAX'->AVP '\n' VAL=TRIM: 'AVE'->AVP,
 *   (CL*,ROW,COL{2})->REC '(' VAL: 'IN_NORTHWESTERN_SECTION'->AVP ')']{6}
 *   []{2} ]+
 * </pre>
 */
class AtpTask124Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "124"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, ItemFilterConditionSpec.sameCol()));

        // MIN '-' MAX '\n' AVE=TRIM, rec '(' IN_NW ')'
        ContentSpec dataCell = new CompoundContentSpec(List.of(
                new CompoundSegment("",
                        AtomicContentSpec.val(ActionSpec.avp("MIN"))),
                new CompoundSegment("-",
                        AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                new CompoundSegment("\\n",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("AVE"), recAve)),
                new CompoundSegment("(",
                        AtomicContentSpec.val(ActionSpec.avp("IN_NORTHWESTERN_SECTION")))
        ), ")");

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(6),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR"))),
                                CellPattern.skip(Quantifier.exactly(2))
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(6),
                                        AtomicContentSpec.val(ActionSpec.avp("MONTH"))),
                                CellPattern.skip(Quantifier.exactly(2))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(Quantifier.exactly(6), dataCell),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                )
        );
    }
}
