package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task25.
 */
class AtpTask25Test extends AtpTaskBase {

    private static final String SEP = "/";

    private static final ItemFilterCondition RIGHT_OF          = (a, c) -> c.rightOf(a).sameSubrow();
    private static final ItemFilterCondition BELOW_STR         = (a, c) -> c.below(a).sameSubtable() && c.below(a).sameCol() && c.sameStr(a);
    private static final ItemFilterCondition SUBROW_AFTER_ANCHOR = (a, c) -> c.sameSubrow(a) && c.cell().col() > a.cell().col() + 1;

    @Override
    protected String taskId() {
        return "25";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.suffix(SEP, ProviderSpec.of(1, RIGHT_OF)),
                                        ActionSpec.rec(ProviderSpec.of(SUBROW_AFTER_ANCHOR)),
                                        ActionSpec.concat(ProviderSpec.of(BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        ).withTransformations(new DelimitedFieldSplit(SEP));
    }
}
