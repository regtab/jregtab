package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 88: explicit subtable with subtable-level ROW-&gt;AVP (inherited); anchor row (once) +
 * continuation rows (one-or-more). Each row has ATTR=SUBSTR(4,1) followed by explicit subrows
 * of non-blank VAL cells and optional BLANK separators.
 * Anchor VAL: BW*-&gt;REC + ROW-&gt;AVP (inherited). Continuation VAL: ROW-&gt;AVP (inherited) only.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_088/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask088Test}
 */
class AtpTask088Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK_COND     =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK_COND =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final ItemFilterConditionSpec BELOW    = ItemFilterConditionSpec.below();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "088"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor substr = new StringExtractor.Substring(4, 5);

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.attr(substr))
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        new CellPattern(NOT_BLANK_COND, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW)),
                                                ActionSpec.avp(ProviderSpec.attr(SAME_ROW))
                                        )),
                                        new CellPattern(BLANK_COND, Quantifier.zeroOrOne(), null)
                                )
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.attr(substr))
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        new CellPattern(NOT_BLANK_COND, Quantifier.oneOrMore(), AtomicContentSpec.val(
                                                ActionSpec.avp(ProviderSpec.attr(SAME_ROW))
                                        )),
                                        new CellPattern(BLANK_COND, Quantifier.zeroOrOne(), null)
                                )
                        )
                )
        );
    }
}
