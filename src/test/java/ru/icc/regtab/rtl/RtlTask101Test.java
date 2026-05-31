package ru.icc.regtab.rtl;

/**
 * Task 101: each cell contains tab-separated values; the first value anchors
 * a REC record collecting the remaining values via the tab-delimited (VAL){'\t'} segment.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_101/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask101Test}
 * <pre>
 * [ [VAL: CL*-&gt;REC '\t' (VAL){'\t'}]+ ]+
 * </pre>
 */
public class RtlTask101Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "101"; }

    @Override
    protected String buildRtl() {
        return "[ [VAL: CL*->REC '\t' (VAL){'\t'}]+ ]+";
    }
}
