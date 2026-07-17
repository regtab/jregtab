package ru.icc.regtab.rtl;

/**
 * Task 97: flat table where each row's anchor VAL collects same-subrow values to the right
 * via RT*->REC, and joins all records of same-string VALs below in the same subcol
 * via (BW&STR)*->JOIN(0,1), dropping key positions {0,1} from each joined record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_097/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask097Test}
 * <pre>
 * [ [VAL: RT*->REC, (BW&STR)*->JOIN(0,1)] [VAL]+ ]+
 * </pre>
 */
public class RtlTask097Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "097"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ "[ [VAL: RT*->REC, (BW&STR)*->JOIN(0,1)] [VAL]+ ]+";
    }
}
