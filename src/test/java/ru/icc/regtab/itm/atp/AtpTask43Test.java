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
 * ATP equivalent of Fluent API Task43.
 */
class AtpTask43Test extends AtpTaskBase {

    private static final ProviderSpec RIGHT_OF_SAME_ROW =
            ProviderSpec.val((a, c) -> c.is.rightOf(a).sameRow());

    private static final ProviderSpec ATTR_SAME_CELL_AS_VALUE =
            ProviderSpec.attr((a, c) -> c.sameCell(a));

    @Override
    protected String taskId() {
        return "43";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec subjectValue = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of(":", AtomicContentSpec.val(
                        ActionSpec.avp(ATTR_SAME_CELL_AS_VALUE)
                ))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(RIGHT_OF_SAME_ROW)
                                )),
                                CellPattern.of(Quantifier.exactly(3), subjectValue)
                        )
                )
        );
    }
}
