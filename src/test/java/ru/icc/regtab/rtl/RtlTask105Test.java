package ru.icc.regtab.rtl;

/**
 * Task 105: table-level cell match condition ('\d+') combined with NCL provider.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_105/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask105Test}
 * <pre>
 * '\d+' ?
 * [ [VAL: NCL*-&gt;REC] [VAL]+ ]
 * [ [VAL]+ ]+
 * </pre>
 */
public class RtlTask105Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "105"; }

    @Override
    protected String buildRtl() {
        return """
                '\\d+' ?
                [ [VAL: NCL*->REC] [VAL]+ ]
                [ [VAL]+ ]+
                """;
    }
}
