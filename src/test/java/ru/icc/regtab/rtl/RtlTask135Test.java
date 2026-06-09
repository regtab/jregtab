package ru.icc.regtab.rtl;

/**
 * Task 135: KSR capacity table with hierarchical location columns and fill.
 * Header row extracts ATTR names uppercased (=UC). Data rows use COL->AVP
 * (row-level actSpec) so each cell's attribute name comes from the column header.
 * First 3 columns may be blank (hierarchical zones/subjects/districts) and use
 * -AV&amp;!BLANK->FILL to inherit the nearest non-blank value above.
 * REC anchor is the last cell (seats), collecting -LT{2} (rooms, KSR) and ROW{4}
 * (eco zone, subject, district, locality).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_135/}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]{3} [VAL]{3} [VAL : (-LT{2},ROW{4})-&gt;REC] ]+
 * </pre>
 */
public class RtlTask135Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "135"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR=UC]+ ]
                [ COL->AVP 
                  [BLANK ? VAL : -AV&!BLANK->FILL | VAL]{3} 
                  [VAL]{3} 
                  [VAL : (-LT{2},ROW{4})->REC]
                ]+
                """;
    }
}
