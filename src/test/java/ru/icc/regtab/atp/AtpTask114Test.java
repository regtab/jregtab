package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 114: fuel consumption table with two skip header rows, compound ORGANIZATION/LOCATION cell,
 * blank-guarded FUEL_CONSUMPTION, and trailing emission columns.
 * REC collects same-row items from cols 0..2 or 4..5 (distributed as two OR alternatives).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_114/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask114Test}
 * <pre>
 * [ []+ ]{2}
 * [ [VAL: 'ORGANIZATION'->AVP ',' VAL=TRIM: 'LOCATION'->AVP]
 *   [VAL: 'YEAR'->AVP] [VAL: 'FUEL_TYPE'->AVP]
 *   [BLANK ? _ | VAL: 'FUEL_CONSUMPTION'->AVP, ROW&(C0..2|C4..5)*->REC]
 *   [VAL: 'SULPHUR_CONTENT'->AVP] [VAL: 'ASH_CONTENT'->AVP] []+ ]+
 * </pre>
 */
class AtpTask114Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "114"; }

    @Override
    protected TablePattern buildPattern() {
        ContentSpec orgLoc = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("ORGANIZATION")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("LOCATION"))));

        // ROW & (C0..2 | C4..5)* distributed: (ROW & C0..2) | (ROW & C4..5)
        ItemFilterConditionSpec rowColRange = ItemFilterConditionSpec.or(
                ItemFilterConditionSpec.and(FilterTerm.SameRow.INSTANCE, new FilterTerm.ColAbsoluteRange(0, 2)),
                ItemFilterConditionSpec.and(FilterTerm.SameRow.INSTANCE, new FilterTerm.ColAbsoluteRange(4, 5)));

        ActionSpec recFuel = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, rowColRange));

        ContentSpec fuelSpec = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("FUEL_CONSUMPTION"), recFuel));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(orgLoc),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("YEAR"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("FUEL_TYPE"))),
                                CellPattern.of(fuelSpec),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("SULPHUR_CONTENT"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("ASH_CONTENT"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
