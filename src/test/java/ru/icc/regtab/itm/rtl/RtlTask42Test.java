package ru.icc.regtab.itm.rtl;

/**
 * Task 42: flat table where each row has a name anchor cell followed by exactly
 * two compound ATTR:VAL cells, each with a same-cell AVP.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask42Test}
 * <pre>
 * [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{2} ]+
 * </pre>
 * Each data row: anchor VAL with empty-literal AVP and unbounded RT*->REC
 * (all values to the right). Then exactly 2 compound ATTR:VAL cells where
 * the VAL part uses CL->AVP (same-cell ATTR as attribute).
 */
public class RtlTask42Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "42"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RT*->REC] [ATTR ":" VAL : CL->AVP]{2} ]+
                """;
    }
}
