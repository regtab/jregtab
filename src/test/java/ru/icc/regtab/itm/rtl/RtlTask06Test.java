package ru.icc.regtab.itm.rtl;

/**
 * Task 06: repeated subtables — first row has a subtable-wide REC anchor
 * then conditional cells; four following rows contain only conditional cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_06/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask06Test}
 * <pre>
 * { [ [VAL : ST*->REC] [(BLANK ? _ | VAL)]+ ]
 *   [ [(BLANK ? _ | VAL)]+ ]{4} }+
 * </pre>
 * First row of each subtable: anchor VAL with REC collecting all same-subtable
 * values (ST, unbounded), followed by one-or-more cells that skip if blank or
 * extract VAL otherwise. Next four rows: only conditional blank-or-VAL cells.
 */
public class RtlTask06Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "06"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ST*->REC] [(BLANK ? _ | VAL)]+ ]
                  [ [(BLANK ? _ | VAL)]+ ]{4} }+
                """;
    }
}
