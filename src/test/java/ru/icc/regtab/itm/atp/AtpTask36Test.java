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
 * ATP equivalent of Fluent API Task36: pivot student blocks (12 rows × 3 cols).
 * First row: name VAL (avp("") + rec col-2 in subtable), subject ATTR, grade VAL (avp left-attr).
 * Next exactly-11 rows: skip, subject ATTR, grade VAL (avp left-attr).
 */
class AtpTask36Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL2 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(2));
    private static final ItemFilterConditionSpec LEFT_OF             = ItemFilterConditionSpec.leftOf();

    @Override
    protected String taskId() {
        return "36";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(SAME_SUBTABLE_COL2))
                                )),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(LEFT_OF))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(11),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(LEFT_OF))
                                ))
                        )
                )
        );
    }
}
