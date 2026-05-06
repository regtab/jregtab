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
 * ATP equivalent of Fluent API Task22.
 */
class AtpTask22Test extends AtpTaskBase {

    private static final ProviderSpec REC_COLS_2_TO_5_COLUMN_MAJOR =
            ProviderSpec.of(ProviderSpec.UNBOUNDED, TraversalOrder.COLUMN_MAJOR,
                    (a, c) -> c.sameSubtable(a) && c.is.in.cols.from(2).to(5));

    @Override
    protected String taskId() {
        return "22";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(REC_COLS_2_TO_5_COLUMN_MAJOR)
                                )),
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
