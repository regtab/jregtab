package ru.icc.regtab.rtl;

/**
 * Task 53: two-row group table with compound attribute names (group header + qualifier).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_053/}
 * <pre>
 * [ [] [AUX]+ ]
 * [ [VAL : ROW*->REC, (BW &amp; STR)->JOIN(0), 'ID'->AVP]
 *   {[ATTR : AV->PREFIX('_')] [VAL : SR->AVP]}+ ]+
 * </pre>
 * Header row: group-name cells (REF, SPECS) are AUX; ID column is a bare skip cell.
 * Data rows come in pairs sharing the same ID. The ID cell is the REC anchor; ROW* collects
 * all VAL cells from the same row, while JOIN(0) (BW &amp; STR) merges the paired row below.
 * Each ATTR qualifier cell gets PREFIX'd with the header cell above it (AV→PREFIX('_')),
 * forming compound names (REF_TP, SPECS_HV, …). Each VAL cell gets AVP from its ATTR sibling
 * in the same explicit subrow (SR→AVP).
 */
public class RtlTask053Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "053"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [AUX]+ ]
                [ [VAL : ROW*->REC, (BW & STR)->JOIN(0), 'ID'->AVP]
                  {[ATTR : AV->PREFIX('_')] [VAL : SR->AVP]}+]+
                """;
    }
}
