package ru.icc.regtab.rtl;

/**
 * Task 69: single non-repeating subtable with SR-&gt;AVP; first row anchors BW*-&gt;REC with
 * explicit subrows of ATTR + tagged VAL#1 (JOIN with same-row #1 items) + tagged VAL#2
 * (JOIN with same-row #2 items); subsequent rows have ATTR + two plain VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_069/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask069Test}
 * <pre>
 * { SR-&gt;AVP
 * [ BW*-&gt;REC { [ATTR] [VAL#'1': ROW&amp;#'1'*-&gt;JOIN][VAL#'2': ROW&amp;#'2'*-&gt;JOIN] }* ]
 * [          { [ATTR] [VAL]{2} }* ]* }
 * </pre>
 * The SR-&gt;AVP subtable-level action propagates attribute lookup from the same subrow.
 * Tagged VAL cells join their same-row peers by tag: #1 cells collect #1-tagged neighbours,
 * #2 cells collect #2-tagged neighbours, merging records without duplicate attributes.
 */
public class RtlTask069Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "069"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                SR->AVP
                [ BW*->REC { [ATTR] [VAL#'1': ROW&#'1'*->JOIN][VAL#'2': ROW&#'2'*->JOIN] }* ]
                [          { [ATTR] [VAL]{2} }* ]*
                """;
    }
}
