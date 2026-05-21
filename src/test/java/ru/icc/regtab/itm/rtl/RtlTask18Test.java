package ru.icc.regtab.itm.rtl;

/**
 * Task 18: repeated subtables with one compound ATTR=VAL header row (collecting
 * subtable-wide values and an AVP) and exactly 15 data rows with ATTR=VAL AVP only.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_18/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask18Test}
 * <pre>
 * { [ [ATTR "=" VAL : ST*->REC, CL->AVP] ] [ [ATTR "=" VAL : CL->AVP] ]{15} }+
 * </pre>
 * Header row: compound ATTR=VAL cell where the VAL part uses ST*->REC (unbounded
 * same-subtable collection) and CL->AVP (same-cell attribute). Each of the 15
 * data rows has a compound ATTR=VAL cell with only CL->AVP (same-cell attribute
 * lookup without further REC).
 */
public class RtlTask18Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "18"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [ATTR "=" VAL : ST*->REC, CL->AVP] ] [ [ATTR "=" VAL : CL->AVP] ]{15} }+
                """;
    }
}
