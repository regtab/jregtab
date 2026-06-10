package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 145: district tourism statistics with region blocks and year columns.
 * Header: 2 ATTR=UC cells + YEAR+ cells. Row-level COL-&gt;AVP inherited.
 * First data column may be blank (fill). Second must be non-blank. Each of
 * the 3 data cells gets 'DATA'-&gt;AVP and REC via ROW{2} + COL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_145/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask145Test}
 * <pre>
 *   [ [ATTR=UC]{2} [VAL : 'YEAR'-&gt;AVP]+ ]
 * { [ [VAL#'LOC' : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL] [!BLANK ? VAL]
 *     [VAL : 'DATA'-&gt;AVP, (ROW{2},COL)-&gt;REC]{3} ]+ }+
 * </pre>
 */
class AtpTask145Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "145"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(2, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(colAvp, fill),
                AtomicContentSpec.val(colAvp));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(2),
                                        AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.valTagged("#LOC",
                                        ActionSpec.avp("LOCATION"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(fillOrVal),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(colAvp)),
                                CellPattern.of(Quantifier.exactly(3),
                                        AtomicContentSpec.val(colAvp, ActionSpec.avp("DATA"), rec))
                        )
                )
        );
    }
}
