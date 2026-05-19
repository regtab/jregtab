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
 * ATP equivalent of Fluent API Task26.
 */
class AtpTask26Test extends AtpTaskBase {

    private static final ProviderSpec COL2_IN_SUBTABLE =
            ProviderSpec.of((a, c) -> c.sameSubtable(a) && c.col(2));

    private static final ProviderSpec ATTR_IN_SAME_ROW =
            ProviderSpec.attr((a, c) -> c.sameSubrow(a));

    @Override
    protected String taskId() {
        return "26";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(COL2_IN_SUBTABLE)
                                )),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ATTR_IN_SAME_ROW)
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(5),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ATTR_IN_SAME_ROW)
                                ))
                        )
                )
        );
    }
}
