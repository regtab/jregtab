package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 115: emission table with two global header rows and explicit subtables.
 * REC on EMISSION: ROW{3} (ORGANIZATION, LOCATION, YEAR) and COL&R1 (POLLUTANT from row 1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_115/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask115Test}
 * <pre>
 * [ []+ ]
 * [ []{7} [VAL: 'POLLUTANT'->AVP]+ ]
 * { [ [VAL: 'ORGANIZATION'->AVP ',' VAL=TRIM: 'LOCATION'->AVP]
 *     [VAL: 'YEAR'->AVP] []{5}
 *     [VAL: 'EMISSION'->AVP, (ROW{3},COL&R1)->REC]+ ]
 *   [ []+ ] }+
 * </pre>
 */
class AtpTask115Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "115"; }

    @Override
    protected TablePattern buildPattern() {
        ContentSpec orgLoc = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("ORGANIZATION")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("LOCATION"))));

        ItemFilterConditionSpec colR1 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));

        ActionSpec recEmission = ActionSpec.rec(
                ProviderSpec.val(3, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, colR1));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(7)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("POLLUTANT")))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(orgLoc),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("YEAR"))),
                                CellPattern.skip(Quantifier.exactly(5)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("EMISSION"), recEmission))
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
