package ru.icc.regtab.itm.rtl;

/**
 * Task 15: flat table where each cell contains a compound value — three
 * space-separated parts each collected via same-cell REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_15/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask15Test}
 * <pre>
 * [ [VAL ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1)] ]+
 * </pre>
 * Each cell holds a compound spec: a plain VAL prefix followed by three
 * space-delimited VAL segments each with REC(1) bound to the same-cell (CL)
 * anchor, splitting a multi-part string into separate attribute-value pairs.
 */
public class RtlTask15Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "15"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1)] ]+
                """;
    }
}
