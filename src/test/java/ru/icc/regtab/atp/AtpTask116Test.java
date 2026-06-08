package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 116: environmental monitoring table.
 * $V1 = [VAL: -AV->PREFIX(', ')], $V2 = [VAL: 'VALUE'->AVP, (ROW,COL&R1..3*,-AV&#'IND')->REC].
 * Row 3 has row-level 'LOCATION'->AVP inherited by all VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_116/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask116Test}
 */
class AtpTask116Test extends AtpTaskBase {

    private static final CellMatchCondition YEAR_COND =
            new CellMatchCondition(new CellPredicate.RegexMatched("20\\d\\d"));

    @Override
    protected String taskId() { return "116"; }

    @Override
    protected TablePattern buildPattern() {
        // $V1: VAL with PREFIX(', ') from reverse-above (any item type, includes AUX from row 2)
        ActionSpec v1Prefix = ActionSpec.prefix(", ",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.above()));
        // In row 3, row-level 'LOCATION'->AVP is inherited first, then local action
        ActionSpec locAvp = ActionSpec.avp("LOCATION").asInherited();

        AtomicContentSpec v1Cell = AtomicContentSpec.val(locAvp, v1Prefix);
        AtomicContentSpec valLocCell = AtomicContentSpec.val(locAvp);

        // $V2: VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC
        ItemFilterConditionSpec colR1to3 = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowAbsoluteRange(1, 3));
        ItemFilterConditionSpec avInd = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, new FilterTerm.Tagged("#IND"));
        AtomicContentSpec v2Cell = AtomicContentSpec.val(
                ActionSpec.avp("VALUE"),
                ActionSpec.rec(
                        ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                        ProviderSpec.val(ProviderSpec.UNBOUNDED, colR1to3),
                        ProviderSpec.val(1, TraversalOrder.REVERSE_ROW_MAJOR, avInd)));

        // Indicator header row: [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+
        ContentSpec indUnit = CompoundContentSpec.of(
                AtomicContentSpec.valTagged("#IND", ActionSpec.avp("INDICATOR")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(ActionSpec.avp("UNIT"))));

        return TablePattern.of(
                // Implicit header subtable: rows 0-3
                SubtablePattern.of(
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("TERRITORY")))
                        ),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())
                        ),
                        // Row 3: LOCATION row with inherited AVP
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.skip(),
                                        CellPattern.of(Quantifier.exactly(4), v1Cell),
                                        CellPattern.of(valLocCell),
                                        CellPattern.skip(),
                                        CellPattern.of(valLocCell),
                                        CellPattern.of(v1Cell),
                                        CellPattern.of(valLocCell),
                                        CellPattern.of(v1Cell),
                                        CellPattern.of(valLocCell),
                                        CellPattern.skip()
                                ),
                                SubrowPattern.of(Quantifier.zeroOrOne(),
                                        CellPattern.of(valLocCell),
                                        CellPattern.of(v1Cell),
                                        CellPattern.of(valLocCell),
                                        CellPattern.skip()
                                )
                        )
                ),
                // Explicit data subtable: {indicator row + data rows}+
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), indUnit)
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.of(YEAR_COND, Quantifier.one(),
                                                AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                                ),
                                SubrowPattern.of(Quantifier.exactly(2),
                                        CellPattern.of(Quantifier.exactly(5), v2Cell),
                                        CellPattern.skip()
                                ),
                                SubrowPattern.of(Quantifier.zeroOrOne(),
                                        CellPattern.of(Quantifier.exactly(3), v2Cell),
                                        CellPattern.skip()
                                )
                        )
                )
        );
    }
}
