package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import java.util.Set;

/**
 * Task 98: headed flat table with one header row (two blank cells + ATTR+ header cells),
 * then data rows where anchor VAL at col 0 collects all right-of cells via RT*->REC,
 * joins same-string rows below via (BW&STR)*->JOIN(0,1), and each VAL at cols 2+
 * carries a COL->AVP action that maps it to its column header attribute.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_098/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask098Test}
 */
class AtpTask098Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF  = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR =
            ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);
    private static final ItemFilterConditionSpec SAME_COL  = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "098"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        // Header row: [] [] [ATTR]+
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())
                        ),
                        // Data rows: [VAL: RT*->REC, (BW&STR)*->JOIN(0,1)] [VAL] [VAL: COL->AVP]{2} [VAL]+
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF)),
                                        ActionSpec.join(Set.of(0, 1), ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_COL))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
