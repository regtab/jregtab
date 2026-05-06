package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task06.
 */
class AtpTask06Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ConditionalContentSpec BLANK_SKIP_OTHERWISE_VAL = new ConditionalContentSpec(
            BLANK,
            AtomicContentSpec.skip(),
            AtomicContentSpec.val());

    private static final ProviderSpec REC_AFTER_ANCHOR = ProviderSpec.of((a, c) -> c.sameSubtable(a));

    @Override
    protected String taskId() {
        return "06";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(REC_AFTER_ANCHOR)
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), BLANK_SKIP_OTHERWISE_VAL)
                        ),
                        RowPattern.of(Quantifier.exactly(4),
                                CellPattern.of(Quantifier.oneOrMore(), BLANK_SKIP_OTHERWISE_VAL)
                        )
                )
        );
    }
}
