package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.CompoundSegment;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

import java.util.List;

/**
 * ATP equivalent of Fluent API Task42.
 */
class AtpTask42Test extends AtpTaskBase {

    private static final ProviderSpec RIGHT_OF_SAME_ROW =
            ProviderSpec.val((a, c) -> c.is.rightOf(a).sameRow());

    private static final ProviderSpec ATTR_SAME_CELL_AS_VALUE =
            ProviderSpec.attr((a, c) -> c.is.in.sameCell(a));

    @Override
    protected String taskId() {
        return "42";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec subjectValue = new CompoundContentSpec(
                List.of(
                        new CompoundSegment("", AtomicContentSpec.attr()),
                        new CompoundSegment(":", AtomicContentSpec.val(
                                ActionSpec.avp(ATTR_SAME_CELL_AS_VALUE)
                        ))
                ),
                ""
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.ctxAttr("")),
                                        ActionSpec.rec(RIGHT_OF_SAME_ROW)
                                )),
                                CellPattern.of(Quantifier.exactly(2), subjectValue)
                        )
                )
        );
    }
}
