package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 144: land plot table — no header row, all columns auto-named ATTR0-N.
 * First cell of each row is REC anchor collecting all row-right cells via RT*.
 * Remaining cells may be blank: fill from nearest non-blank cell above.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_144/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask144Test}
 * <pre>
 * [ [VAL : RT*-&gt;REC] [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]+ ]+
 * </pre>
 */
class AtpTask144Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "144"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.rightOf()));

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(fill),
                AtomicContentSpec.val());

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(rec)),
                                CellPattern.of(Quantifier.oneOrMore(), fillOrVal)
                        )
                )
        );
    }
}
