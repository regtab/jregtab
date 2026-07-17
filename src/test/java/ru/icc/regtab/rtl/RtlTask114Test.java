package ru.icc.regtab.rtl;

/**
 * Task 114: fuel consumption table with two header rows, compound organization/location cell,
 * and trailing emission columns (skipped). Blank fuel-consumption cells are skipped.
 * REC on FUEL_CONSUMPTION collects same-row items from cols 0..2 or 4..5 via a disjunctive
 * column range constraint (ORGANIZATION, LOCATION, YEAR, FUEL_TYPE, SULPHUR_CONTENT, ASH_CONTENT).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_114/}
 * <pre>
 * [ []+ ]{2}
 * [ [VAL: 'ORGANIZATION'-&gt;AVP ',' VAL=TRIM: 'LOCATION'-&gt;AVP]
 *   [VAL: 'YEAR'-&gt;AVP]
 *   [VAL: 'FUEL_TYPE'-&gt;AVP]
 *   [BLANK ? _ | VAL: 'FUEL_CONSUMPTION'-&gt;AVP, ROW&amp;(C0..2|C4..5)*-&gt;REC]
 *   [VAL: 'SULPHUR_CONTENT'-&gt;AVP]
 *   [VAL: 'ASH_CONTENT'-&gt;AVP]
 *   []+ ]+
 * </pre>
 */
public class RtlTask114Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "114"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ []+ ]{2}
                [ [VAL: 'ORGANIZATION'->AVP ',' VAL=TRIM: 'LOCATION'->AVP] 
                  [VAL: 'YEAR'->AVP] 
                  [VAL: 'FUEL_TYPE'->AVP]
                  [BLANK ? _ | VAL: 'FUEL_CONSUMPTION'->AVP, ROW&(C0..2|C4..5)*->REC]
                  [VAL: 'SULPHUR_CONTENT'->AVP]
                  [VAL: 'ASH_CONTENT'->AVP]
                  []+
                ]+
                """;
    }
}
