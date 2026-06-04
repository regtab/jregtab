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

import java.util.List;

/**
 * Task 103: vertical comparison table — rows alternate between ATTR headers and value pairs.
 * Table-level ST->AVP assigns each VAL its attribute from the same subtable ATTR.
 * Both VAL cells in the first implicit subtable anchor column records (COL*->REC);
 * each explicit subtable contributes one attribute per column via inherited ST->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_103/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask103Test}
 * <pre>
 * ST->AVP
 * [ [ATTR]+ ]
 * [ [VAL: COL*->REC]+ ]
 * { [ [ATTR]+ ]
 *   [ [VAL]+ ] }+
 * </pre>
 */
class AtpTask103Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_COL      = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();

    @Override
    protected String taskId() { return "103"; }

    @Override
    protected TablePattern buildPattern() {
        // ST->AVP: lookup ATTR in same physical subtable
        ActionSpec stAvp = ActionSpec.avp(ProviderSpec.attr(SAME_SUBTABLE));
        // COL*->REC: collect all same-column VAL items (below and above, kind=VAL)
        ActionSpec colRec = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_COL));

        // Anchor VAL: both COL*->REC and ST->AVP
        AtomicContentSpec anchorVal = AtomicContentSpec.val(colRec, stAvp);
        // Data VAL in explicit subtables: ST->AVP only
        AtomicContentSpec dataVal = AtomicContentSpec.val(stAvp);

        return TablePattern.of(
                // implicit subtable: [ [ATTR]+ ] [ [VAL: COL*->REC]+ ]
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())),
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), anchorVal))
                ),
                // explicit subtables { [ [ATTR]+ ] [ [VAL]+ ] }+
                new SubtablePattern(null, Quantifier.oneOrMore(), List.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr())),
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), dataVal))
                ))
        );
    }
}
