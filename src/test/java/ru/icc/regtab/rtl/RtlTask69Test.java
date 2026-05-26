package ru.icc.regtab.rtl;

/**
 * Task 69:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_69/}
 */
public class RtlTask69Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "69"; }

    @Override
    protected String buildRtl() {
        return """
                { SR->AVP 
                [ { [ATTR] [VAL: ^(BW | C+3)*->REC]{2} } { [ATTR] [VAL]{2} } ]
                [ { [ATTR] [VAL]{2} }{2} ]+ }
                """;
    }
}
