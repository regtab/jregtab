package ru.icc.regtab.rtl;

/**
 * Task 143: tourist-recreational cluster table with region blocks and fill.
 * Header row extracts ATTR names uppercased (=UC). Each subtable starts with
 * a LOCATION header row (VAL tagged #'LOC', then any cells). Data rows use
 * COL->AVP (row-level actSpec). First column (cluster) may be blank:
 * blank cells use -AV&amp;!BLANK->FILL and create REC via ROW*; non-blank cells
 * create REC via ROW* directly. Second column may also be blank (fill only,
 * no REC). Last 2 columns are always non-blank.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_143/}
 * <pre>
 *   [ [ATTR=UC]+ ]
 * { [ [VAL#'LOC' : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, ROW*-&gt;REC | VAL : ROW*-&gt;REC]
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL] [!BLANK ? VAL]{2} ]+ }+
 * </pre>
 */
public class RtlTask143Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "143"; }

    @Override
    protected String buildRtl() {
        return """
                  [ [ATTR=UC]+ ]
                { [ [VAL#'LOC' : 'LOCATION'->AVP] []+ ]
                  [ COL->AVP
                    [BLANK ? VAL : -AV&!BLANK->FILL, ROW*->REC | VAL : ROW*->REC]
                    [BLANK ? VAL : -AV&!BLANK->FILL | VAL] [!BLANK ? VAL]{2} ]+ }+
                """;
    }
}
