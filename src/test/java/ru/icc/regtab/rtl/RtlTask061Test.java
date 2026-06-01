package ru.icc.regtab.rtl;

/**
 * Task 61: repeating rows where each cell is a three-segment compound: an anchor VAL
 * (CL*-&gt;REC, 'A'-&gt;AVP), a space-separated VAL ('B'-&gt;AVP), and a further space-separated
 * VAL ('N'-&gt;AVP).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_061/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask061Test}
 * <pre>
 * [ [VAL: CL*-&gt;REC, 'A'-&gt;AVP ' ' VAL: 'B'-&gt;AVP ' ' VAL: 'N'-&gt;AVP]+ ]+
 * </pre>
 * The anchor collects same-cell items (CL*) into a record and labels itself with literal
 * attribute 'A'. The two following segments within the same cell add named attributes
 * 'B' and 'N' respectively, each preceded by a space delimiter.
 */
public class RtlTask061Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "061"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: CL*->REC, 'A'->AVP ' ' VAL: 'B'->AVP ' ' VAL: 'N'->AVP]+ ]+
                """;
    }
}
