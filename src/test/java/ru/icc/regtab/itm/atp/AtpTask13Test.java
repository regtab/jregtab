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
 * ATP equivalent of Fluent API Task13.
 */
class AtpTask13Test extends AtpTaskBase {

    private static final ProviderSpec AVP_SAME_COL = ProviderSpec.one((a, c) -> c.sameCol(a));

    private static final ProviderSpec SAME_ROW_COL2 =
            ProviderSpec.of(1, (a, c) -> c.sameRow(a) && c.col(2));
    private static final ProviderSpec SAME_ROW_COL4 =
            ProviderSpec.of(1, (a, c) -> c.sameRow(a) && c.col(4));
    private static final ProviderSpec SAME_ROW_COL1 =
            ProviderSpec.of(1, (a, c) -> c.sameRow(a) && c.col(1));
    private static final ProviderSpec SAME_ROW_COL3 =
            ProviderSpec.of(1, (a, c) -> c.sameRow(a) && c.col(3));


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
                                        ActionSpec.avp(AVP_SAME_COL),
                                        ActionSpec.rec(SAME_ROW_COL2, SAME_ROW_COL4, SAME_ROW_COL1, SAME_ROW_COL3)
                                )),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val(
                                        ActionSpec.avp(AVP_SAME_COL)
                                )),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
