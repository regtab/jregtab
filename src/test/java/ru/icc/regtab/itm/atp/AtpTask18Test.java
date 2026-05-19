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
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task18.
 */
class AtpTask18Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_CELL      = (a, c) -> c.sameCell(a);
    private static final ItemFilterCondition BELOW_SUBTABLE = (a, c) -> c.below(a).sameSubtable();

    @Override
    protected String taskId() {
        return "18";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec firstRow = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.rec(ProviderSpec.val(BELOW_SUBTABLE)),
                        ActionSpec.avp(ProviderSpec.of(1, SAME_CELL))
                ))
        );

        CompoundContentSpec otherRows = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of("=", AtomicContentSpec.val(
                        ActionSpec.avp(ProviderSpec.of(1, SAME_CELL))
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
