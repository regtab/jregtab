package ru.icc.regtab.rtl;

/**
 * Task 90: one header row of VAL cells (asterisks stripped via REPL), below data rows are AUX.
 * BW*->SUFFIX('/') appends each column's data values to the header value; ()->REC emits the
 * resulting string as a single-field record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_090/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask090Test}
 * <pre>
 * [ [VAL=REPL('\*','') : BW*-&gt;SUFFIX('/'), ()-&gt;REC]+ ]
 * [ [AUX]+ ]+
 * </pre>
 */
public class RtlTask090Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "090"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL=REPL('\\*','') : BW*->SUFFIX('/'), ()->REC]+ ]
                [ [AUX]+ ]+
                """;
    }
}
