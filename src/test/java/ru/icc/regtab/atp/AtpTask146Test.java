package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 146: indicator table with 5 year columns and trailing skip columns.
 * First cell of data rows gets COL-&gt;AVP (indicator name from column header).
 * Next 5 cells get 'DATA'-&gt;AVP and REC via ROW + COL. Remaining cells skipped.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_146/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask146Test}
 * <pre>
 * [ [ATTR=UC] [VAL : 'YEAR'-&gt;AVP]{5} []+ ]
 * [ [VAL : COL-&gt;AVP] [VAL : 'DATA'-&gt;AVP, (ROW,COL)-&gt;REC]{5} []+ ]+
 * </pre>
 */
class AtpTask146Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "146"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(colAvp)),
                                CellPattern.of(Quantifier.exactly(5),
                                        AtomicContentSpec.val(ActionSpec.avp("DATA"), rec)),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
