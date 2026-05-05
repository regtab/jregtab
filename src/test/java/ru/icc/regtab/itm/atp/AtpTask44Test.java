package ru.icc.regtab.itm.atp;

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

import java.util.List;

/**
 * ATP equivalent of Fluent API Task44.
 */
class AtpTask44Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());
    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ProviderSpec FIRST_IN_SAME_ROW = ProviderSpec.of(1, (a, c) -> c.is.in.sameRow(a));
    private static final ProviderSpec SAME_CELL = ProviderSpec.of((a, c) -> c.is.in.sameCell(a));

    @Override
    protected String taskId() {
        return "44";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec commaPair = new CompoundContentSpec(
                List.of(
                        new CompoundSegment("", AtomicContentSpec.val()),
                        new CompoundSegment(",", AtomicContentSpec.val(
                                ActionSpec.rec(SAME_CELL)
                        ))
                ),
                ""
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(FIRST_IN_SAME_ROW)
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.exactly(2), null),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), commaPair)
                        )
                )
        );
    }
}
