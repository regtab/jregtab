package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 107: cross-tabulation with multi-level column headers (H) and row headers (S).
 * Header rows use FILL to propagate non-blank values right into blank cells.
 * Data cells collect all H items from the same column and S items from the same row into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_107/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask107Test}
 * <pre>
 * [ [BLANK]+ [!BLANK ? VAL#'H'] [(BLANK ? VAL#'H': -(LT &amp; !BLANK)-&gt;FILL | VAL#'H')]+ ]+
 * {
 * [ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL &amp; #'H')*,(ROW &amp; #'S')*)-&gt;REC]+ ]
 * [ [BLANK ? VAL#'S': SC-&gt;FILL]+ ['\\D.*' ? VAL#'S']+ ['\\d+' ? VAL: ((COL &amp; #'H')*,(ROW &amp; #'S')*)-&gt;REC]+ ]*
 * }+
 * </pre>
 */
class AtpTask107Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition NON_DIGIT = new CellMatchCondition(new CellPredicate.RegexMatched("\\D.*"));
    private static final CellMatchCondition DIGIT     = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));

    private static final ItemFilterConditionSpec LT_NOT_BLANK = ItemFilterConditionSpec.and(
            FilterTerm.LeftOf.INSTANCE, FilterTerm.NotBlank.INSTANCE);
    private static final ItemFilterConditionSpec COL_H = ItemFilterConditionSpec.and(
            FilterTerm.SameCol.INSTANCE, new FilterTerm.Tagged("#H"));
    private static final ItemFilterConditionSpec ROW_S = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#S"));
    private static final ItemFilterConditionSpec SAME_SUBCOL = ItemFilterConditionSpec.sameSubcol();

    @Override
    protected String taskId() { return "107"; }

    @Override
    protected TablePattern buildPattern() {
        // -(LT & !BLANK)->FILL  — reverse-row-major fill from nearest non-blank left-of cell
        ActionSpec fillReverse = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, LT_NOT_BLANK));

        // ((COL & #'H')*, (ROW & #'S')*)->REC
        ActionSpec recColRow = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, COL_H),
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_S));

        // SC->FILL
        ActionSpec scFill = ActionSpec.fill("", ProviderSpec.any(1, SAME_SUBCOL));

        // (BLANK ? VAL#'H': -(LT & !BLANK)->FILL | VAL#'H')
        ConditionalContentSpec condH = new ConditionalContentSpec(
                BLANK,
                AtomicContentSpec.valTagged("#H", fillReverse),
                AtomicContentSpec.valTagged("#H"));

        return TablePattern.of(
                // implicit header subtable: [ [BLANK]+ [!BLANK ? VAL#'H'] [(BLANK ? ...)]+ ]+
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.valTagged("#H")),
                                CellPattern.of(Quantifier.oneOrMore(), condH)
                        )
                ),
                // explicit data subtable: { row1 row2* }+
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.one(),
                                CellPattern.of(NON_DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#S")),
                                CellPattern.of(DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.val(recColRow))
                        ),
                        RowPattern.of(Quantifier.zeroOrMore(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#S", scFill)),
                                CellPattern.of(NON_DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.valTagged("#S")),
                                CellPattern.of(DIGIT, Quantifier.oneOrMore(), AtomicContentSpec.val(recColRow))
                        )
                )
        );
    }
}
