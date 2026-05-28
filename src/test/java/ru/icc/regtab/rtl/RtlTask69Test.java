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
        // return """
        //         { SR->AVP 
        //         [ { [ATTR] [VAL: ^(BW | C+3)*->REC]{2} } { [ATTR] [VAL]{2} } ]
        //         [ { [ATTR] [VAL]{2} }{2} ]+ }
        //         """;

        // return """
        //         { SR->AVP
        //         [ { [ATTR] [VAL: BW*->REC, C+3->JOIN, C+6->JOIN]{2} }+ ]
        //         [ { [ATTR] [VAL]{2} }+ ]+ }
        //         """;

        return """
                { SR->AVP
                [ { BW*->REC [ATTR] [VAL#'1': (ROW & #'1')*->JOIN][VAL#'2': (ROW & #'2')*->JOIN] }+ ]
                [ { [ATTR] [VAL]{2} }+ ]+ }
                """;
    }
}
