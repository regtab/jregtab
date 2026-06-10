package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 138: SPNA visitor statistics with two-row header (attr names + years).
 * First cell of data rows gets COL-&gt;AVP (SPNA name from column header).
 * Remaining cells use C1-&gt;AVP (attribute name from absolute column 1 header).
 * REC: ROW (SPNA name) and COL (year).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_138/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask138Test}
 * <pre>
 * [ [ATTR=UC]{2} []+ ]
 * [ [] [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [VAL : COL-&gt;AVP] [VAL : C1-&gt;AVP, (ROW,COL)-&gt;REC]+ ]+
 * </pre>
 */
class AtpTask138Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "138"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()));

        ActionSpec c1Avp = ActionSpec.avp(
                ProviderSpec.attr(ItemFilterConditionSpec.bare(new FilterTerm.ColExact(1))));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(2),
                                        AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(colAvp)),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(c1Avp, rec))
                        )
                )
        );
    }
}
