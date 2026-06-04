package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 102: multi-section equipment passport table. First row is a header (ATTR + skips).
 * Repeating explicit subtables: the first row anchors each equipment name as VAL (COL->AVP,
 * ST*->REC); continuation rows start with a blank skip cell. Both first and continuation
 * rows contain paired subrows (attr|skip, val|skip) using SR->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_102/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask102Test}
 * <pre>
 * [ [ATTR] []+ ]
 * { [ [!BLANK? VAL: COL->AVP, ST*->REC] { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]
 *   [ [BLANK?]                          { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]+ }+
 * </pre>
 */
class AtpTask102Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_COL      = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();
    private static final ItemFilterConditionSpec SAME_SUBROW   = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() { return "102"; }

    @Override
    protected TablePattern buildPattern() {
        // (BLANK ? _ | ATTR)  — if blank → skip, else → ATTR
        ConditionalContentSpec condAttr = new ConditionalContentSpec(
                BLANK, AtomicContentSpec.skip(), AtomicContentSpec.attr());

        // (BLANK ? _ | VAL: SR->AVP)  — if blank → skip, else → VAL with SR->AVP
        ConditionalContentSpec condVal = new ConditionalContentSpec(
                BLANK, AtomicContentSpec.skip(),
                AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))));

        // { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+
        SubrowPattern attrValSubrow = SubrowPattern.of(Quantifier.oneOrMore(),
                CellPattern.of(condAttr),
                CellPattern.of(condVal));

        // [ [!BLANK? VAL: COL->AVP, ST*->REC]  { (BLANK?_|ATTR) (BLANK?_|VAL:SR->AVP) }+ ]
        RowPattern firstRow = new RowPattern(null, Quantifier.one(), java.util.List.of(
                SubrowPattern.of(
                        new CellPattern(NOT_BLANK, Quantifier.one(),
                                AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_COL)),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE))
                                ))
                ),
                attrValSubrow
        ));

        // [ [BLANK?]  { (BLANK?_|ATTR) (BLANK?_|VAL:SR->AVP) }+ ]+
        RowPattern contRow = new RowPattern(null, Quantifier.oneOrMore(), java.util.List.of(
                SubrowPattern.of(
                        new CellPattern(BLANK, Quantifier.one(), null)
                ),
                attrValSubrow
        ));

        return TablePattern.of(
                // implicit subtable: [ [ATTR] []+ ]
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.skip())
                        )
                ),
                // explicit subtable+: { firstRow contRow+ }+
                new SubtablePattern(null, Quantifier.oneOrMore(), java.util.List.of(firstRow, contRow))
        );
    }
}
