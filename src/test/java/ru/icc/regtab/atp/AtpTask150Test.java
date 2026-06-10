package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 150: population statistics table with location rows and year columns.
 * Header row: one ATTR cell (column label), then YEAR-&gt;AVP values. Data rows:
 * first cell LOCATION-&gt;AVP; remaining cells use ST-&gt;AVP (attribute name from
 * same-subtable ATTR cell) and REC via ROW + COL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_150/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask150Test}
 * <pre>
 * [ [ATTR] [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [VAL : 'LOCATION'-&gt;AVP] [VAL : ST-&gt;AVP, (ROW,COL)-&gt;REC]+ ]+
 * </pre>
 */
class AtpTask150Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "150"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec stAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameSubtable()));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("LOCATION"))),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(stAvp, rec))
                        )
                )
        );
    }
}
