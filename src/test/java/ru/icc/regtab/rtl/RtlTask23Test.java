package ru.icc.regtab.rtl;

/**
 * Task 23: repeated subtables of exactly 3 rows, each with four cells combining
 * AVP, REC, JOIN(0), and SUFFIX actions across same-row and below-same-string providers.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_23/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask23Test}
 * <pre>
 * { [ [VAL : ''->AVP, SR*->REC, (BW & STR)*->JOIN(0)] [ATTR : RT->SUFFIX] [AUX] [VAL : SR->AVP] ]{3} }+
 * </pre>
 * Each of the 3 rows has: (1) VAL anchor with empty-literal AVP, unbounded same-subrow
 * REC, and unbounded below-same-string JOIN(0); (2) ATTR cell that appends the adjacent
 * AUX to its value via RT->SUFFIX; (3) an AUX cell; (4) a VAL cell whose attribute is
 * looked up from the same-subrow ATTR (SR->AVP).
 */
public class RtlTask23Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "23"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, SR*->REC, (BW & STR)*->JOIN(0)] [ATTR : RT->SUFFIX] [AUX] [VAL : SR->AVP] ]{3} }+
                """;
    }
}
