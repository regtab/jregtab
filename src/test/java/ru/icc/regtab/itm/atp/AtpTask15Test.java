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
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * ATP equivalent of Fluent API Task15.
 */
class AtpTask15Test extends AtpTaskBase {

    private static final ProviderSpec FIRST_IN_SAME_CELL = ProviderSpec.of(1, (a, c) -> c.sameCell(a));

    @Override
    protected String taskId() {
        return "15";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec compoundSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_CELL))),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_CELL))),
                CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_CELL)))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(compoundSpec)
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }
}
