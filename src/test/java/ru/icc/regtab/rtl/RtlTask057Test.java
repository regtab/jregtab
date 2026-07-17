package ru.icc.regtab.rtl;

/**
 * Task 57: repeating two-cell rows — an anchor VAL (RT-&gt;REC, single right) and a compound
 * cell containing two trimmed VAL segments separated by a literal dash.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_057/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask057Test}
 * <pre>
 * [ [VAL: RT-&gt;REC] [VAL=TRIM '-' VAL=TRIM] ]+
 * </pre>
 * The anchor cell in each row collects the single right-neighbour cell into its record
 * (RT-&gt;REC). The second cell is a compound of two trimmed values joined by a dash delimiter.
 */
public class RtlTask057Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "057"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL: RT->REC] [VAL=TRIM '-' VAL=TRIM] ]+
                """;
    }
}
