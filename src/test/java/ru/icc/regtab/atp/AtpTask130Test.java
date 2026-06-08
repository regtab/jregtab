package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 130: cross-tabulation with YEAR header, explicit subrow pairs {MPC+AVE}{2}.
 * REC on AVE: ROW (INDICATOR), -LT{2} (MPC_MIN, MPC_MAX), COL (YEAR), @'UNIT'='MG/DM3'.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_130/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask130Test}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{4} [] ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'->AVP]
 *   { ['\s*-?\s*' ? _ | VAL: 'MPC_MIN'->AVP '-' VAL: 'MPC_MAX'->AVP]
 *     [VAL: 'AVE'->AVP, (ROW,-LT{2},COL,@'UNIT'='MG/DM3')->REC] }{2}
 *   [] ]+
 * </pre>
 */
class AtpTask130Test extends AtpTaskBase {

    private static final CellMatchCondition DASH_OPT =
            new CellMatchCondition(new CellPredicate.RegexMatched("\\s*-?\\s*"));

    @Override
    protected String taskId() { return "130"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr04 = new StringExtractor.Substring(0, 4);

        ActionSpec recAve = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.leftOf()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()),
                ProviderSpec.ctxAvp("UNIT", "MG/DM3"));

        ContentSpec mpcCell = new ConditionalContentSpec(DASH_OPT,
                AtomicContentSpec.skip(),
                CompoundContentSpec.of(
                        AtomicContentSpec.val(ActionSpec.avp("MPC_MIN")),
                        CompoundContentSpec.Segment.of("-",
                                AtomicContentSpec.val(ActionSpec.avp("MPC_MAX")))));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(4),
                                        AtomicContentSpec.val(substr04, ActionSpec.avp("YEAR"))),
                                CellPattern.skip()
                        ),
                        RowPattern.of(CellPattern.skip(Quantifier.oneOrMore())),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR")))
                                ),
                                SubrowPattern.of(Quantifier.exactly(2),
                                        CellPattern.of(mpcCell),
                                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AVE"), recAve))
                                ),
                                SubrowPattern.of(Quantifier.one(),
                                        CellPattern.skip()
                                )
                        )
                )
        );
    }
}
