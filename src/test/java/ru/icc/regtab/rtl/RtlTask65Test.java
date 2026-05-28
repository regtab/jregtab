package ru.icc.regtab.rtl;

/**
 * Task 65: repeating rows — anchor VAL appends a suffix from its single right neighbour
 * (RT-&gt;SUFFIX(', ')) and collects it into REC (RT-&gt;REC); the second cell is a compound
 * of AUX + ', ' + VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_65/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask65Test}
 * <pre>
 * [ [VAL: RT-&gt;SUFFIX(', '), RT-&gt;REC] [AUX ', ' VAL] ]+
 * </pre>
 * The anchor cell decorates itself with the right neighbour's text as a comma-space suffix
 * before linking it into the record. The compound second cell joins an auxiliary label with
 * a value via the literal separator.
 */
public class RtlTask65Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "65"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: RT->SUFFIX(', '), RT->REC] [AUX ', ' VAL] ]+
                """;
    }
}
