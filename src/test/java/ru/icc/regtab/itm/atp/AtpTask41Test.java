package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CompoundContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task41.
 */
class AtpTask41Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());
    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ProviderSpec SAME_CELL = ProviderSpec.val((a, c) -> c.sameCell(a));
    private static final ProviderSpec RIGHT_SAME_ROW = ProviderSpec.val((a, c) -> c.rightOf(a).sameRow());

    @Override
    protected String taskId() {
        return "41";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec pairValSpec = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.fill("",ProviderSpec.ctxAux("")),ActionSpec.rec(SAME_CELL,RIGHT_SAME_ROW)),
                CompoundContentSpec.Segment.of("", AtomicContentSpec.val())
        );
       
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), pairValSpec),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), 
                                        AtomicContentSpec.val(
                                                ActionSpec.rec(RIGHT_SAME_ROW, ProviderSpec.ctxAux(""))
                                        )
                                ),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        )
                )
        );
    }

}
