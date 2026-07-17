package ru.icc.regtab.rtl;

/**
 * Task 131: MPC exceedance frequency table with YEAR header.
 * condContSpec skips cells containing '*'; otherwise assigns MPC_EXCEEDING_FREQUENCY.
 * REC collects POLLUTANT/MPC (ROW{2}), col-4 cell (ROW&amp;C4), and YEAR from row 1 (COL&amp;R1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_131/}
 * <pre>
 * [ []+ ]
 * [ []{2} [VAL=SUBSTR(0,4): 'YEAR'-&gt;AVP]{2} [] ]
 * [ [VAL: 'POLLUTANT'-&gt;AVP] [VAL: 'MPC'-&gt;AVP]
 *   [~'*' ? _ | VAL: 'MPC_EXCEEDING_FREQUENCY'-&gt;AVP, (ROW{2},ROW&amp;C4,COL&amp;R1)-&gt;REC]{2}
 *   [] ]+
 * </pre>
 */
public class RtlTask131Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "131"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ []+ ]
                [ []{2} [VAL=SUBSTR(0,4): 'YEAR'->AVP]{2} [] ]
                [ [VAL: 'POLLUTANT'->AVP] [VAL: 'MPC'->AVP]
                  [~'*' ? _ | VAL: 'MPC_EXCEEDING_FREQUENCY'->AVP, (ROW{2},ROW&C4,COL&R1)->REC]{2}
                  [] 
                ]+
                """;
    }
}
