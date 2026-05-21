package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task04.
 */
class AtpTask04Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() {
        return "04";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(1, ProviderSpec.val(1, SAME_SUBROW))
                                ))
                        )
                ));
    }
}
