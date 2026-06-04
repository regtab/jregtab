package ru.icc.regtab.rtl;

/**
 * Task 88: explicit subtable with subtable-level ROW-&gt;AVP; an anchor row and one-or-more
 * continuation rows each have an ATTR=SUBSTR(4,1) cell followed by explicit subrows of
 * non-blank VAL cells separated by optional BLANK cells. Anchor VAL cells collect all
 * values below via BW*-&gt;REC; ROW-&gt;AVP (inherited) links each VAL to its row's ATTR name.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_088/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask088Test}
 * <pre>
 * { ROW-&gt;AVP
 *   [ [ATTR=SUBSTR(4,1)] { [!BLANK? VAL: BW*-&gt;REC]+ [BLANK]? }+ ]
 *   [ [ATTR=SUBSTR(4,1)] { [!BLANK? VAL]+ [BLANK]? }+ ]+ }
 * </pre>
 * "NameA" -&gt; "A" (SUBSTR extracts one char starting at position 4).
 * BW* on each anchor-row VAL collects all below-column values into one record per column group.
 */
public class RtlTask088Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "088"; }

    @Override
    protected String buildRtl() {
        return """
                ROW->AVP 
                [ [ATTR=SUBSTR(4,1)] { [!BLANK? VAL: BW*->REC]+ [BLANK]? }+ ]
                [ [ATTR=SUBSTR(4,1)] { [!BLANK? VAL]+ [BLANK]? }+ ]+
                """;
    }
}
