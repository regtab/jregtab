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
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;

/**
 * ATP equivalent of Fluent API Task23.
 */
class AtpTask23Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec RIGHT_OF    = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() {
        return "23";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.exactly(3),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(SAME_SUBROW)),
                                        ActionSpec.concat(ProviderSpec.val(BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.attr(
                                        ActionSpec.suffix("", ProviderSpec.of(1, TraversalOrder.ROW_MAJOR, RIGHT_OF))
                                )),
                                CellPattern.of(AtomicContentSpec.aux()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        )
                )
        );
    }
}
