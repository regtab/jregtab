package ru.icc.regtab.rtl;

/**
 * Task 62: repeating subtables with a fixed header row (skip + VALs), one-or-more data rows
 * conditioned on not matching "x" (VAL anchor + VALs collecting above and left-of into REC),
 * and an optional trailer row of "x"-matching cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_062/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask062Test}
 * <pre>
 * { [       []    [VAL]+ ]
 *   [ !"x"? [VAL] [VAL: (AV, LT)-&gt;REC]+ ]+
 *   [       ['x'?]+ ]? }+
 * </pre>
 * The header row has a skip cell and column-header VALs. Data rows are guarded by !"x"?
 * (first cell does not match the literal "x"): the first cell is a plain VAL, subsequent
 * cells anchor their record via same-row-above (AV) and left-of (LT) providers.
 */
public class RtlTask062Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "062"; }

    @Override
    protected String buildRtl() {
        return """
            { [       []    [VAL]+ ]
              [ !"x"? [VAL] [VAL: (AV, LT)->REC]+ ]+
              [       ['x'?]+ ]? }+
             """;
    }
}
