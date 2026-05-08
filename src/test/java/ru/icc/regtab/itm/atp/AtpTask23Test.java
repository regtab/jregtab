package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
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

    private static final ProviderSpec REC_VALUE_COL =
            ProviderSpec.val((a, c) -> c.sameRow(a));

    private static final ProviderSpec SUFFIX_SOFTWARE_RIGHT =
            ProviderSpec.aux(1, TraversalOrder.ROW_MAJOR, (a, c) -> c.rightOf(a).sameRow());

    private static final ProviderSpec SAME_ROW_ATTR =
            ProviderSpec.attr((a, c) -> c.sameRow(a));

    private static final ProviderSpec SAME_ID_BELOW =
            ProviderSpec.of((a, c) -> c.below(a).sameCol() && c.sameStr(a));

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
                                        ActionSpec.rec(REC_VALUE_COL),
                                        ActionSpec.concat(SAME_ID_BELOW)
                                )),
                                CellPattern.of(AtomicContentSpec.attr(
                                        ActionSpec.suffix("", SUFFIX_SOFTWARE_RIGHT)
                                )),
                                CellPattern.of(AtomicContentSpec.aux()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(SAME_ROW_ATTR)
                                ))
                        )
                )
        );
    }
}
