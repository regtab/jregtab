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
 * ATP equivalent of Fluent API Task12.
 */
class AtpTask12Test extends AtpTaskBase {

    private static final ProviderSpec AMOUNT_COLUMN = ProviderSpec.of((a, c) -> c.is.in.col(5));

    @Override
    protected String taskId() {
        return "12";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(AMOUNT_COLUMN)
                                )),
                                CellPattern.skip(Quantifier.exactly(4)),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.skip(Quantifier.exactly(5)),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
