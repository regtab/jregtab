package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 104: each row is collapsed into a single space-joined string. The first cell
 * anchors a singleton record (()->REC) and collects all cells to its right as a
 * space-separated suffix (RT*->SUFFIX(' ')). The remaining cells are AUX (consumed
 * but contribute only their string value to the suffix).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_104/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask104Test}
 * <pre>
 * [ [VAL: RT*->SUFFIX(' '), ()->REC] [AUX]+ ]+
 * </pre>
 */
class AtpTask104Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "104"; }

    @Override
    protected TablePattern buildPattern() {
        // RT*->SUFFIX(' '): append right-of items space-separated to anchor value
        ActionSpec suffixRt = ActionSpec.suffix(" ", ProviderSpec.any(ProviderSpec.UNBOUNDED, RIGHT_OF));
        // ()->REC: singleton record (no provider items)
        ActionSpec emptyRec = ActionSpec.rec();

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(suffixRt, emptyRec)),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())
                        )
                )
        );
    }
}
