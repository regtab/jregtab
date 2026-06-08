package ru.icc.regtab.rtl;

/**
 * Task 116: environmental monitoring table with four leading header rows followed by
 * repeating subtables of indicator+data rows.
 * <p>
 * Header rows:
 * <ul>
 *   <li>Row 0: ignored (skip all cells).</li>
 *   <li>Row 1: TERRITORY — one label per column group (Subjects of Russian Federation, BPT, CEZ BPT).</li>
 *   <li>Row 2: sub-group names as AUX items (e.g. "Irkutsk Oblast", "Republic of Buryatia").</li>
 *   <li>Row 3: specific location labels. Where the sub-group (row 2) differs from the specific
 *       label, {@code -AV-&gt;PREFIX(', ')} prepends the sub-group, producing e.g.
 *       "Irkutsk Oblast, Total by subject". Where they match, a plain {@code VAL} is used
 *       (e.g. "Zabaykalsky Krai", "Irkutsk Oblast"). Summary/total columns are skipped.</li>
 * </ul>
 * Each VALUE cell anchors REC collecting: YEAR (same row via ROW), TERRITORY and LOCATION
 * from the same column at rows 1–3 (COL&amp;R1..3*), and INDICATOR tagged #IND from above
 * (-AV&amp;#'IND').
 * <p>
 * Data section: two mandatory groups — Subjects (5 VALUE cols + 1 skip) and BPT
 * (5 VALUE cols + 1 skip) — and an optional CEZ BPT group (3 VALUE cols + 1 skip).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_116/}
 * <pre>
 * [ []+ ]
 * [ [] [VAL: 'TERRITORY'-&gt;AVP]+ ]
 * [ [AUX]+ ]
 * [ 'LOCATION'-&gt;AVP []
 *     [VAL: -AV-&gt;PREFIX(', ')]{4} [VAL] []
 *     [VAL] [VAL: -AV-&gt;PREFIX(', ')] [VAL] [VAL: -AV-&gt;PREFIX(', ')] [VAL] []
 *     { [VAL] [VAL: -AV-&gt;PREFIX(', ')] [VAL] [] }? ]
 * { [ [VAL#'IND': 'INDICATOR'-&gt;AVP ',' VAL: 'UNIT'-&gt;AVP]+ ]
 *   [ ['20\\d\\d' ? VAL: 'YEAR'-&gt;AVP]
 *     { [VAL: 'VALUE'-&gt;AVP, (ROW, COL&amp;R1..3*, -AV&amp;#'IND')-&gt;REC]{5} [] }{2}
 *     { [VAL: 'VALUE'-&gt;AVP, (ROW, COL&amp;R1..3*, -AV&amp;#'IND')-&gt;REC]{3} [] }?
 *   ]+
 * }+
 * </pre>
 */
public class RtlTask116Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "116"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [] [VAL: 'TERRITORY'->AVP]+ ]
                [ [AUX]+ ]
                [ 'LOCATION'->AVP [] [VAL: -AV->PREFIX(', ')]{4} [VAL] [] 
                                     [VAL] [VAL: -AV->PREFIX(', ')] [VAL] 
                                     [VAL: -AV->PREFIX(', ')] [VAL] [] 
                                     { [VAL] [VAL: -AV->PREFIX(', ')] [VAL] [] }? ]
                { [ [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+ ]
                  [ ['20\\d\\d' ? VAL: 'YEAR'->AVP]
                    { [VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]{5} [] }{2}
                    { [VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]{3} [] }? 
                  ]+
                }+
                """;
    }
}
