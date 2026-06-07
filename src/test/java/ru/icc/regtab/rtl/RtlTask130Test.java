package ru.icc.regtab.rtl;

/**
 * Task 130: cross-tabulation with YEAR header, explicit subrow pairs {[MPC][AVE]}{2}.
 * REC on AVE collects INDICATOR (ROW), MPC_MIN/MPC_MAX left of anchor (-LT{2}),
 * YEAR in same column (COL), and constant UNIT='MG/DM3' (@'UNIT'='MG/DM3').
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_130/}
 * <pre>
 * [ [] [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]{4} [] ]
 * [ []+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP]
 *   { ['\s*-?\s*' ? _ | VAL: 'MPC_MIN'-&gt;AVP '-' VAL: 'MPC_MAX'-&gt;AVP]
 *     [VAL: 'AVE'-&gt;AVP, (ROW,-LT{2},COL,@'UNIT'='MG/DM3')-&gt;REC] }{2}
 *   [] ]+
 * </pre>
 */
public class RtlTask130Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "130"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL=SUBSTR(0,4): 'YEAR'->AVP]{4} [] ]
                [ []+ ]
                [ [VAL: 'INDICATOR'->AVP]
                  { ['\\s*-?\\s*' ? _ | VAL: 'MPC_MIN'->AVP '-' VAL: 'MPC_MAX'->AVP]
                    [VAL: 'AVE'->AVP, (ROW,-LT{2},COL,@'UNIT'='MG/DM3')->REC] }{2}
                  []
                ]+
                """;
    }
}
