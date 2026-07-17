package ru.icc.regtab.rtl;

/**
 * Task 55: repeating single-cell rows where each cell is a VAL anchor collecting
 * same-cell siblings (CL*) into REC, followed by a comma-delimited group of VALs.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_055/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask055Test}
 * <pre>
 * [ [VAL: CL*-&gt;REC ',' (VAL){','}] ]+
 * </pre>
 * Each row contains one compound cell: the anchor VAL collects all same-cell items via
 * CL*-&gt;REC, then a comma separator followed by a comma-delimited repetition of VAL values.
 */
public class RtlTask055Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "055"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL: CL*->REC ',' (VAL){','}] ]+
                """;
    }
}
