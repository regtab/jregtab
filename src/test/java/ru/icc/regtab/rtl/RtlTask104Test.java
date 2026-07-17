package ru.icc.regtab.rtl;

/**
 * Task 104: each row is collapsed into a single space-joined string. The first cell
 * anchors a singleton record (()->REC) and collects all cells to its right as a
 * space-separated suffix (RT*->SUFFIX(' ')). The remaining cells are AUX (consumed
 * but contribute only their string value to the suffix).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_104/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask104Test}
 * <pre>
 * [ [VAL: RT*->SUFFIX(' '), ()->REC] [AUX]+ ]+
 * </pre>
 */
public class RtlTask104Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "104"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ "[ [VAL: RT*->SUFFIX(' '), ()->REC] [AUX]+ ]+";
    }
}
