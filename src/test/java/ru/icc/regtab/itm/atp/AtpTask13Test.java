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

/**
 * ATP equivalent of Fluent API Task13.
 */
class AtpTask13Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBCOLUMN    = (a, c) -> c.sameSubcol(a);

    private static final ItemFilterCondition SAME_SUBROW_COL2  = (a, c) -> c.sameSubrow(a) && c.col(2);
    private static final ItemFilterCondition SAME_SUBROW_COL4  = (a, c) -> c.sameSubrow(a) && c.col(4);
    private static final ItemFilterCondition SAME_SUBROW_COL1  = (a, c) -> c.sameSubrow(a) && c.col(1);
    private static final ItemFilterCondition SAME_SUBROW_COL3  = (a, c) -> c.sameSubrow(a) && c.col(3);

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
                                        ActionSpec.avp(ProviderSpec.one(SAME_SUBCOLUMN)),
                                        ActionSpec.rec(ProviderSpec.of(1, SAME_SUBROW_COL2), ProviderSpec.of(1, SAME_SUBROW_COL4),
                                                       ProviderSpec.of(1, SAME_SUBROW_COL1), ProviderSpec.of(1, SAME_SUBROW_COL3))
                                )),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.one(SAME_SUBCOLUMN))
                                )),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
