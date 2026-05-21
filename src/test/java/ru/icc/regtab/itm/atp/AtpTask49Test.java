package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task49.
 */
class AtpTask49Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW    = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec SAME_SUBCOLUMN = ItemFilterConditionSpec.sameSubcol();

    @Override
    protected String taskId() {
        return "49";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.one()),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(2, ProviderSpec.val(1, SAME_SUBROW), ProviderSpec.val(1, SAME_SUBCOLUMN))
                                ))
                        )
                ));
    }
}
