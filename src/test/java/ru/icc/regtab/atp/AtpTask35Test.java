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
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 35: repeated subtables where the header row is matched by a glob on "*Company"
 * and its anchor value is asterisk-stripped before collecting all values below via REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_35/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask35Test}
 */
class AtpTask35Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    private static final CellMatchCondition COMPANY_ROW     = new CellMatchCondition(new CellPredicate.Contains("*Company"));
    private static final CellMatchCondition NOT_COMPANY_ROW = new CellMatchCondition(new CellPredicate.NotContains("*Company"));

    @Override
    protected String taskId() {
        return "35";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(COMPANY_ROW, Quantifier.one(), AtomicContentSpec.val(
                                        new StringExtractor.Replaced("\\*", ""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_COMPANY_ROW, Quantifier.one(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
