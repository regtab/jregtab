package ru.icc.regtab.rtl;

/**
 * Task 42: flat table where each row has a name anchor cell followed by exactly
 * two compound ATTR:VAL cells, each with a same-cell AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_042/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask042Test}
 * <pre>
 * [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{2} ]+
 * </pre>
 * Each data row: anchor VAL with empty-literal AVP and unbounded RT*->REC
 * (all values to the right). Then exactly 2 compound ATTR:VAL cells where
 * the VAL part uses CL->AVP (same-cell ATTR as attribute).
 */
public class RtlTask042Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "042"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{2} ]+
                """;
    }
}
