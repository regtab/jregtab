package ru.icc.regtab.rtl;

/**
 * Task 66: repeating rows of conditional cells — if the cell contains '=', it is parsed
 * as a compound VAL=TRIM (CL-&gt;REC) '=' VAL=TRIM; otherwise a plain VAL with empty-literal
 * REC anchor.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_066/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask066Test}
 * <pre>
 * [ [~'=' ? VAL=TRIM: CL-&gt;REC '=' VAL=TRIM | VAL: ''-&gt;REC]+ ]+
 * </pre>
 * The ~'=' condition matches cells whose text contains the literal '='. Matching cells
 * produce a key=value compound; non-matching cells produce a single VAL anchored by an
 * empty-string context provider.
 */
public class RtlTask066Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "066"; }

    @Override
    protected String buildRtl() {
        return """
                [ [~'=' ? VAL=TRIM: CL->REC '=' VAL=TRIM | VAL: ''->REC]+ ]+
                """;
    }
}
