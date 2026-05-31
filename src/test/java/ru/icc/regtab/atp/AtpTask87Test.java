package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 87: repeating rows with one-or-more explicit subrows — anchor VAL (RT*-&gt;REC)
 * and exactly 2 plain VAL cells per subrow.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_87/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask87Test}
 */
class AtpTask87Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "87"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF))
                                        )),
                                        CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val())
                                )
                        )
                )
        );
    }
}
