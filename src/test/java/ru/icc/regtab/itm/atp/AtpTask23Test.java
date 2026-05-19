package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;

/**
 * ATP equivalent of Fluent API Task23.
 */
class AtpTask23Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBROW = (a, c) -> c.sameSubrow(a);
    private static final ItemFilterCondition RIGHT_OF    = (a, c) -> c.rightOf(a).sameSubrow();
    private static final ItemFilterCondition BELOW_STR   = (a, c) -> c.below(a).sameSubtable() && c.below(a).sameCol() && c.sameStr(a);

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
                                        ActionSpec.concat(ProviderSpec.of(BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.attr(
                                        ActionSpec.suffix("", ProviderSpec.aux(1, TraversalOrder.ROW_MAJOR, RIGHT_OF))
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
