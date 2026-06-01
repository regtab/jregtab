package ru.icc.regtab.rtl;

/**
 * Task 43: flat table where each row has a name anchor cell followed by exactly
 * three compound ATTR:VAL cells, each with a same-cell AVP (same as task 42
 * but with three attribute-value pairs instead of two).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_043/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask043Test}
 * <pre>
 * [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{3} ]+
 * </pre>
 * Each data row: anchor VAL with empty-literal AVP and unbounded RT*->REC.
 * Then exactly 3 compound ATTR:VAL cells where the VAL part uses CL->AVP
 * (same-cell ATTR as attribute).
 */
public class RtlTask043Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "043"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{3} ]+
                """;
    }
}
