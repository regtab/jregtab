package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task31.
 */
class AtpTask31Test extends AtpTaskBase {

    private static final ProviderSpec BELOW_SAME_COL =
            ProviderSpec.val((a, c) -> c.below(a).sameCol() && c.sameSubtable(a));

    @Override
    protected String taskId() {
        return "31";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(BELOW_SAME_COL)
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(4),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(
                                CellPattern.skip()
                        )
                )
        );
    }
}
