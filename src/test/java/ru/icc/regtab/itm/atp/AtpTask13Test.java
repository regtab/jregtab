package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.FilterTerm;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 13: header row with five ATTR cells; data rows use AVP for five columns
 * and REC referencing four specific column positions in the same row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_13/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask13Test}
 */
class AtpTask13Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBCOLUMN    = ItemFilterConditionSpec.sameSubcol();

    private static final ItemFilterConditionSpec SAME_SUBROW_COL2  = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(2));
    private static final ItemFilterConditionSpec SAME_SUBROW_COL4  = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(4));
    private static final ItemFilterConditionSpec SAME_SUBROW_COL1  = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(1));
    private static final ItemFilterConditionSpec SAME_SUBROW_COL3  = ItemFilterConditionSpec.and(FilterTerm.SameSubrow.INSTANCE, new FilterTerm.ColExact(3));

    @Override
    protected String taskId() {
        return "13";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.exactly(5), AtomicContentSpec.attr()),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBCOLUMN)),
                                        ActionSpec.rec(ProviderSpec.val(1, SAME_SUBROW_COL2), ProviderSpec.val(1, SAME_SUBROW_COL4),
                                                       ProviderSpec.val(1, SAME_SUBROW_COL1), ProviderSpec.val(1, SAME_SUBROW_COL3))
                                )),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBCOLUMN))
                                )),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
