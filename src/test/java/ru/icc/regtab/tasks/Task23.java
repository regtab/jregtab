package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;

/**
 * Task 23 (Foofah exp0_30): pivot blocks of three rows (same id) into one wide record — column headers are
 * product+software (suffix), values are numeric. {@code rows().exactly(3)} repeats one row layout for each block;
 * the applier infers subtable stride {@code n} from that single row type.
 * <p>
 * Uses ITM named cell-derived providers: {@link ProviderSpec#val} for {@code rec} (J = value items; numeric column
 * after removing the id anchor), {@link ProviderSpec#aux} for {@code suffix} on the attribute anchor (ordering picks
 * the software cell), and {@code O_avp} with predicate κ only — applier attaches Υ<sub>tbl</sub><sup>attr</sup>.
 */
public final class Task23 extends TaskBase {

    /** Υ<sub>tbl</sub><sup>val</sup>: same-row numeric value (only other VALUE in the row once id anchor is removed). */
    private static final ProviderSpec REC_VALUE_COL =
            ProviderSpec.val((a, c) -> c.sameRow(a));

    private static final ItemFilterCondition SAME_ID_BELOW =
            (a, c) -> c.below(a).sameCol() && c.sameStr(a);

    /** Υ<sub>tbl</sub><sup>aux</sup>: software cell is right of product; row-major order yields col 2 before col 3. */
    private static final ProviderSpec SUFFIX_SOFTWARE_RIGHT =
            ProviderSpec.aux((a, c) -> c.rightOf(a).sameRow(), TraversalOrder.ROW_MAJOR, 1);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().exactly(3)
                .cells().one().val()
                .actions().avp("").rec(REC_VALUE_COL).concat(SAME_ID_BELOW)
                .cells().one().attr()
                .actions().suffix(SUFFIX_SOFTWARE_RIGHT)
                .cells().one().aux()
                .cells().one().val()
                .actions().avp((a, c) -> c.sameRow(a))
                .apply(syntax);
    }
}
