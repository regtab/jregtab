package ru.icc.regtab.rtl;

/**
 * Task 85: fixed 3×3 grid with three anchors at absolute row/column positions — each
 * anchor VAL references a specific cell by (Rn &amp; Cm) absolute coordinates.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_085/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask085Test}
 * <pre>
 * [ [VAL: (R2 &amp; C1)-&gt;REC ] [VAL]                  [VAL]                  ]
 * [ []                      [VAL: (R0 &amp; C2)-&gt;REC]  []                     ]
 * [ []                      [VAL]                  [VAL: (R0 &amp; C1)-&gt;REC]  ]
 * </pre>
 * The anchor at (row 0, col 0) collects the cell at absolute (row 2, col 1).
 * The anchor at (row 1, col 1) collects the cell at absolute (row 0, col 2).
 * The anchor at (row 2, col 2) collects the cell at absolute (row 0, col 1).
 */
public class RtlTask085Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "085"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: (R2 & C1)->REC ] [VAL]                 [VAL]                 ]
                [ []                     [VAL: (R0 & C2)->REC] []                    ]
                [ []                     [VAL]                 [VAL: (R0 & C1)->REC] ]
                """;
    }
}
