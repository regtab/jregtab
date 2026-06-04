package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 96: two header rows set up three attributes (A at col 0, B and C both at col 1),
 * then each data subtable has two rows: first row with two VALs (col 0 gets A via
 * inherited COL->AVP, col 1 gets B via the same), second row with one VAL that resolves
 * C via explicit (COL&R1)->AVP.  ST*->REC anchors at col-0 VAL collecting the subtable.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_096/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask096Test}
 */
class AtpTask096Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_COL      = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();
    // (COL & R1): same column AND absolute row == 1
    private static final ItemFilterConditionSpec COL_AND_R1    =
            ItemFilterConditionSpec.and(FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(1));

    @Override
    protected String taskId() { return "096"; }

    @Override
    protected TablePattern buildPattern() {
        // Row A, col 0: VAL with ST*->REC (local) + COL->AVP (inherited from row)
        AtomicContentSpec recCell = AtomicContentSpec.val(
                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE)),
                ActionSpec.avp(ProviderSpec.attr(SAME_COL)).asInherited()
        );
        // Row A, col 1: VAL with COL->AVP (inherited from row)
        AtomicContentSpec avpCell = AtomicContentSpec.val(
                ActionSpec.avp(ProviderSpec.attr(SAME_COL)).asInherited()
        );
        // Row B, col 1: VAL with explicit (COL&R1)->AVP
        AtomicContentSpec r1Cell = AtomicContentSpec.val(
                ActionSpec.avp(ProviderSpec.attr(COL_AND_R1))
        );

        return TablePattern.of(
                // Implicit subtable: 2 header rows
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.attr())),
                        RowPattern.of(CellPattern.skip(), CellPattern.of(AtomicContentSpec.attr()))
                ),
                // Explicit subtable, one or more: 2 data rows each
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(CellPattern.of(recCell), CellPattern.of(avpCell)),
                        RowPattern.of(CellPattern.skip(), CellPattern.of(r1Cell))
                )
        );
    }
}
