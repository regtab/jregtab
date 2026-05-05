package ru.icc.regtab.itm.atp;

import java.util.List;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.CompoundSegment;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task48.
 */
class AtpTask48Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    private static final ProviderSpec SAME_SUBTABLE_COL1 = ProviderSpec.val((a, c) ->
            c.is.in.sameSubtable(a) && c.is.in.col(1));
    private static final ProviderSpec SAME_CELL_ATTR = ProviderSpec.attr((a, c) -> c.is.in.sameCell(a));

    @Override
    protected String taskId() {
        return "48";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec telFaxSpec = new CompoundContentSpec(
                List.of(
                        new CompoundSegment("", AtomicContentSpec.attr()),
                        new CompoundSegment(":", AtomicContentSpec.val(ActionSpec.avp(SAME_CELL_ATTR)))
                ),
                ""
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.ctxAttr("")),
                                        ActionSpec.rec(SAME_SUBTABLE_COL1)
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), telFaxSpec)
                        ),
                        RowPattern.of(
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), telFaxSpec)
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        )
                )
        );
    }
}
