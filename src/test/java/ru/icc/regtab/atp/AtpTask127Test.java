package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 127: complex cross-tabulation with two symmetric blocks of MIN-MAX\nAVE data cells.
 * Block 1 REC: CL* (MIN,MAX), ROW{3} (HYDROBIONT_GROUP,TIME,AREA?), COL, COL&R2, ROW&C5.
 * Block 2 REC: CL* (MIN,MAX), ROW{2}, ROW&C6, COL, COL&R2, ROW&C10.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_127/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask127Test}
 */
class AtpTask127Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "127"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        // Indicator subheader: VAL: -AV->PREFIX, 'INDICATOR'->AVP (any item type — includes AUX)
        AtomicContentSpec indCell = AtomicContentSpec.val(
                ActionSpec.prefix("", ProviderSpec.any(1,
                        TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.above())),
                ActionSpec.avp("INDICATOR"));

        ContentSpec hgUnit = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp("HYDROBIONT_GROUP")),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp("UNIT"))));

        ItemFilterConditionSpec colR2  = ItemFilterConditionSpec.and(
                FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(2));
        ItemFilterConditionSpec rowC5  = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(5));
        ItemFilterConditionSpec rowC6  = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(6));
        ItemFilterConditionSpec rowC10 = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(10));

        ActionSpec recBlock1 = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(3, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()),
                ProviderSpec.val(1, colR2),
                ProviderSpec.val(1, rowC5));

        ActionSpec recBlock2 = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCell()),
                ProviderSpec.val(2, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, rowC6),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()),
                ProviderSpec.val(1, colR2),
                ProviderSpec.val(1, rowC10));

        ContentSpec block1Cell = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                CompoundContentSpec.of(
                        AtomicContentSpec.val(ActionSpec.avp("MIN")),
                        CompoundContentSpec.Segment.of("-",
                                AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                        CompoundContentSpec.Segment.of("\\n",
                                AtomicContentSpec.val(ActionSpec.avp("AVE"), recBlock1))));

        ContentSpec block2Cell = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                CompoundContentSpec.of(
                        AtomicContentSpec.val(ActionSpec.avp("MIN")),
                        CompoundContentSpec.Segment.of("-",
                                AtomicContentSpec.val(ActionSpec.avp("MAX"))),
                        CompoundContentSpec.Segment.of("\\n",
                                AtomicContentSpec.val(ActionSpec.avp("AVE"), recBlock2))));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())),
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.skip()
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.skip(),
                                        CellPattern.of(Quantifier.exactly(3), indCell),
                                        CellPattern.skip()
                                )
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(hgUnit),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("TIME"))),
                                CellPattern.of(Quantifier.exactly(3), block1Cell),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AREA"))),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("TIME"))),
                                CellPattern.of(Quantifier.exactly(3), block2Cell),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AREA")))
                        )
                )
        );
    }
}
