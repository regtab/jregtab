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
 * ATP equivalent of Fluent API Task01.
 */
class AtpTask01Test extends AtpTaskBase {

    @Override
    protected String taskId() {
        return "01";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.of((a, c) -> c.is.in.sameSubtable(a)))
                                )),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val()),
                                CellPattern.skip(Quantifier.oneOrMore())
                        )
                )
        );
    }
}
