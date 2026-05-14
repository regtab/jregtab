package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task18.
 */
class AtpTask18Test extends AtpTaskBase {

    private static final ProviderSpec AVP_SAME_CELL = ProviderSpec.of(1, (a, c) -> c.sameCell(a));
    private static final ProviderSpec REC_VALUES_BELOW =
            ProviderSpec.val((a, c) -> c.below(a).sameSubtable());

    @Override
    protected String taskId() {
        return "18";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec firstRow = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.rec(REC_VALUES_BELOW),
                        ActionSpec.avp(AVP_SAME_CELL)
                ))
        );

        CompoundContentSpec otherRows = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.avp(AVP_SAME_CELL)
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(firstRow)
                        ),
                        RowPattern.of(Quantifier.exactly(15),
                                CellPattern.of(otherRows)
                        )
                )
        );
    }
}
