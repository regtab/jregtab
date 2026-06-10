package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 140: protected areas visitor statistics — flat table, no fill needed.
 * Row-level COL-&gt;AVP inherited. First cell is REC anchor collecting ROW*.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_140/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask140Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [VAL : ROW*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
class AtpTask140Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "140"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameRow()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE))),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(colAvp, rec)),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
