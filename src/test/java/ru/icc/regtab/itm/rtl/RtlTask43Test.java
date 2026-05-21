package ru.icc.regtab.itm.rtl;

/**
 * Task 43: flat table where each row has a name anchor cell followed by exactly
 * three compound ATTR:VAL cells, each with a same-cell AVP (same as task 42
 * but with three attribute-value pairs instead of two).
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask43Test}
 * <pre>
 * [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{3} ]+
 * </pre>
 * Each data row: anchor VAL with empty-literal AVP and unbounded RT*->REC.
 * Then exactly 3 compound ATTR:VAL cells where the VAL part uses CL->AVP
 * (same-cell ATTR as attribute).
 */
public class RtlTask43Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "43"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{3} ]+
                """;
    }
}
