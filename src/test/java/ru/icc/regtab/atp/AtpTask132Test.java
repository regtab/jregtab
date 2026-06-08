package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 132: pollutant deposition table with explicit subtables per pollutant.
 * REC on VALUE: ROW (LOCATION), COL&R1 (YEAR), ST&C0&#'IND' (POLLUTANT), ST&C1&#'UNIT'.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_132/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask132Test}
 * <pre>
 * [ []+ ]
 * [ []{2} [VAL: 'YEAR'->AVP]{2} []{2} ]
 * { [ [VAL#'IND': 'POLLUTANT'->AVP] [!BLANK ? VAL#'UNIT': 'UNIT'->AVP] []{4} ]
 *   [ [VAL: 'LOCATION'->AVP] [BLANK]
 *     ['\s*-?\s*' ? _ | VAL: 'VALUE'->AVP,
 *     (ROW,COL&R1,ST&C0&#'IND',ST&C1&#'UNIT')->REC]{2}
 *     []{2} ]+ }+
 * </pre>
 */
class AtpTask132Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "132"; }

    @Override
    protected TablePattern buildPattern() {
        ItemFilterConditionSpec colR1 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));
        ItemFilterConditionSpec stC0ind = ItemFilterConditionSpec.and(
                FilterTerm.SameSubtable.INSTANCE,
                new FilterTerm.ColExact(0),
                new FilterTerm.Tagged("#IND"));
        ItemFilterConditionSpec stC1unit = ItemFilterConditionSpec.and(
                FilterTerm.SameSubtable.INSTANCE,
                new FilterTerm.ColExact(1),
                new FilterTerm.Tagged("#UNIT"));

        ActionSpec recValue = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, colR1),
                ProviderSpec.val(1, stC0ind),
                ProviderSpec.val(1, stC1unit));

        ContentSpec valueSpec = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp("VALUE"), recValue));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.exactly(2),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR"))),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.valTagged("#IND",
                                        ActionSpec.avp("POLLUTANT"))),
                                CellPattern.of(NOT_BLANK, Quantifier.one(),
                                        AtomicContentSpec.valTagged("#UNIT",
                                                ActionSpec.avp("UNIT"))),
                                CellPattern.skip(Quantifier.exactly(4))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("LOCATION"))),
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.of(Quantifier.exactly(2), valueSpec),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                )
        );
    }
}
