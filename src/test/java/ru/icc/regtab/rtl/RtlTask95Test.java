package ru.icc.regtab.rtl;

/**
 * Task 95: repeated subtables where each cell is a compound ATTR=VAL; the first
 * row collects same-subcol values below via BW*->REC, and a CL->AVP inherited
 * at subtable level resolves the attribute from the same cell for every row.
 * Exactly 2 data rows follow the header row in each subtable.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_95/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask95Test}
 * <pre>
 * { CL->AVP
 *   [ [ATTR '=' VAL: BW*->REC]+ ]
 *   [ [ATTR '=' VAL]+ ]{2} }+
 * </pre>
 */
public class RtlTask95Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "95"; }

    @Override
    protected String buildRtl() {
        return """
                { CL->AVP
                  [ [ATTR '=' VAL: BW*->REC]+ ]
                  [ [ATTR '=' VAL]+ ]{2} }+
                """;
    }
}
