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
 * ATP equivalent of Fluent API Task50.
 */
class AtpTask50Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    private static final ProviderSpec SAME_ROW = ProviderSpec.val((a, c) -> c.is.in.sameRow(a));
    private static final ProviderSpec SAME_ROW_ATTR = ProviderSpec.attr((a, c) -> c.is.in.sameRow(a));
    private static final ProviderSpec SAME_YEAR_BELOW = ProviderSpec.val((a, c) ->
            c.is.below(a).sameCol() && c.has.sameStr(a));

    @Override
    protected String taskId() {
        return "50";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.ctxAttr("")),
                                        ActionSpec.rec(SAME_ROW),
                                        ActionSpec.concat(SAME_YEAR_BELOW)
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.attr()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(SAME_ROW_ATTR)
                                ))
                        )
                )
        );
    }
}
