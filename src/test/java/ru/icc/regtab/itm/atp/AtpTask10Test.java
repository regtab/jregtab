package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task10.
 */
class AtpTask10Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ProviderSpec SAME_ROW_REST = ProviderSpec.of((a, c) -> c.sameRow(a));

    @Override
    protected String taskId() {
        return "10";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.zeroOrMore(),
                                CellPattern.skip(Quantifier.exactly(4)),
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.skip(),
                                CellPattern.skip(Quantifier.exactly(2))
                        ),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(SAME_ROW_REST)
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null)
                        )
                )
        );
    }
}
