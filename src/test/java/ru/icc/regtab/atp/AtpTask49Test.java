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
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 49: cross-table unpivot with non-blank guards — a skip+header row followed
 * by data rows where each data cell references both row and column anchors via REC(2).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_49/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask49Test}
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
