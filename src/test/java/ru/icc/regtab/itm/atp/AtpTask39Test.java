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
 * ATP equivalent of Fluent API Task39: compound cell — price VAL (rec same-cell),
 * separator " / ", bedrooms VAL, separator "br", rest SKIP.
 */
class AtpTask39Test extends AtpTaskBase {

    private static final ProviderSpec SAME_CELL = ProviderSpec.of((a, c) -> c.sameCell(a));

    @Override
    protected String taskId() {
        return "39";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec priceBedroomSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.rec(SAME_CELL)),
                CompoundContentSpec.Segment.of(" / ", AtomicContentSpec.val()),
                CompoundContentSpec.Segment.of("br", AtomicContentSpec.skip())
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(priceBedroomSpec)
                        )
                )
        );
    }
}
