package ru.icc.regtab.itm.rtl;

/**
 * Task 29: flat table where each physical row is divided into an explicit 6-cell
 * header subrow and one-or-more 4-cell data subrows with a composite REC anchor.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask29Test}
 * <pre>
 * [ [VAL]{6} { [VAL : (ROW{6}, RT*)->REC(6)] [VAL]{3} }+ ]+
 * </pre>
 * Each row starts with exactly 6 plain VAL cells (the header subrow). Then one-or-more
 * explicit subrows follow: an anchor VAL with REC(6) using providers ROW{6} (6 values
 * from the same logical row) and RT* (unbounded values to the right), plus exactly 3
 * plain VAL cells.
 */
public class RtlTask29Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "29"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL]{6} { [VAL : (ROW{6}, RT*)->REC(6)] [VAL]{3} }+ ]+
                """;
    }
}
