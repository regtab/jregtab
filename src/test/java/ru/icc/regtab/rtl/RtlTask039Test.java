package ru.icc.regtab.rtl;

/**
 * Task 39: flat table where each cell is a compound value — a price part
 * collected via same-cell REC, a bedroom count, and a trailing skip part.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_039/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask039Test}
 * <pre>
 * [ [VAL : CL*->REC " / " VAL "br" _] ]+
 * </pre>
 * Each cell contains a compound spec: a VAL anchor with CL*->REC (unbounded
 * same-cell collection, splitting the price part), followed by the literal
 * separator " / " and a bedrooms VAL, then the literal "br" and a skipped
 * remainder.
 */
public class RtlTask039Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "039"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : CL*->REC " / " VAL "br" _] ]+
                """;
    }
}
