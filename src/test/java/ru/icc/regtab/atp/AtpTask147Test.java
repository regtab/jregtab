package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 147: cross-tab tourism statistics with two header rows and fill.
 * First 2 data columns may be blank (fill from above + direct COL-&gt;AVP).
 * Third data column non-blank with COL-&gt;AVP. Next 5 cells: DATA + REC via
 * ROW{3} + COL{2} (from both header rows). Trailing cells skipped.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_147/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask147Test}
 * <pre>
 * [ [ATTR=UC]{3} [VAL : 'INDICATOR'-&gt;AVP]+ ]
 * [ []{3} [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, COL-&gt;AVP | VAL : COL-&gt;AVP]{2}
 *   [VAL : COL-&gt;AVP] [VAL : 'DATA'-&gt;AVP, (ROW{3},COL{2})-&gt;REC]{5} []+ ]+
 * </pre>
 */
class AtpTask147Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "147"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()));

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(3, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, ItemFilterConditionSpec.sameCol()));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(fill, colAvp),
                AtomicContentSpec.val(colAvp));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(3),
                                        AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("INDICATOR")))
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(3)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.exactly(2), fillOrVal),
                                CellPattern.of(AtomicContentSpec.val(colAvp)),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(ActionSpec.avp("DATA"), rec)),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
