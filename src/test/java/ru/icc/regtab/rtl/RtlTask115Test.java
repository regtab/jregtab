package ru.icc.regtab.rtl;

/**
 * Task 115: emissions table with two global header rows and explicit subtables.
 * Each subtable covers two consecutive rows: the first (coal) generates records,
 * the second (fuel oil) is consumed by the all-skip row pattern.
 * REC on EMISSION collects same-row items (ROW{3}) and the same-column pollutant header from row 1 (COL&amp;R1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_115/}
 * <pre>
 * [ []+ ]
 * [ []{7} [VAL: 'POLLUTANT'-&gt;AVP]+ ]
 * { [ [VAL: 'ORGANIZATION'-&gt;AVP ',' VAL=TRIM: 'LOCATION'-&gt;AVP]
 *     [VAL: 'YEAR'-&gt;AVP] []{5}
 *     [VAL: 'EMISSION'-&gt;AVP, (ROW{3},COL&amp;R1)-&gt;REC]+ ]
 *   [ []+ ] }+
 * </pre>
 */
public class RtlTask115Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "115"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ []{7} [VAL: 'POLLUTANT'->AVP]+ ]
                { [ [VAL: 'ORGANIZATION'->AVP ',' VAL=TRIM: 'LOCATION'->AVP]
                    [VAL: 'YEAR'->AVP] []{5}
                    [VAL: 'EMISSION'->AVP, (ROW{3},COL&R1)->REC]+]
                  [ []+ ]
                }+
                """;
    }
}
