package ru.icc.regtab.rtl;

/**
 * Task 100: cells contain literal-\n-separated values; the first cell anchors
 * three REC records collecting values at positions 0, 1, 2 from the right-of cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_100/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask100Test}
 * <pre>
 * [ [VAL: (RT&amp;P0)*-&gt;REC '\n' VAL: (RT&amp;P1)*-&gt;REC '\n' VAL: (RT&amp;P2)*-&gt;REC]
 *   [VAL '\n' VAL '\n' VAL]{2} ]+
 * </pre>
 * The separator '\n' (backslash + n, 2 chars) matches the literal escape sequence
 * used as a field separator inside each cell in input_*.csv.
 */
public class RtlTask100Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "100"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: (RT&P0)*->REC '\\n' VAL: (RT&P1)*->REC '\\n' VAL: (RT&P2)*->REC]
                  [VAL '\\n' VAL '\\n' VAL]{2} ]+
                """;
    }
}
