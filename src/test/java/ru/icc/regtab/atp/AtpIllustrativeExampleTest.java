package ru.icc.regtab.atp;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Implements the illustrative example from Section VI of the paper
 * "RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure".
 *
 * <p>The example defines a class C of tables listing the numbers of departures
 * operated by airlines from airports in certain months. Each table has the form:
 * <pre>
 *         | AIRLINE₁ | … | AIRLINEₙ
 * AIRPORT₁ | ND MON   | … | ND MON
 *    ⋮    |    ⋮     | ⋱ |   ⋮
 * AIRPORTₘ | ND MON   | … | ND MON
 * </pre>
 *
 * <p>The target schema is S = ⟨ND, AIRLINE, AIRPORT, MON⟩ where ND is the
 * number of departures, AIRLINE is the airline code, AIRPORT is the airport
 * code, and MON is the month.
 *
 * <p>Pattern structure (Section VI-A):
 * <ul>
 *   <li>One subtable pattern P_st^1 with two row patterns:</li>
 *   <li>P_row^1 (header): skip empty first cell + one-or-more airline-code cells</li>
 *   <li>P_row^2 (data, one-or-more): airport-code cell + one-or-more compound cells
 *       whose text has the form "ND MON"</li>
 * </ul>
 *
 * <p>Item providers for the rec action on each ND item (S_prov^1–3, Section VI-A):
 * <ul>
 *   <li>S_prov^1: VAL, k=1, COLUMN_MAJOR — airline code in the same column (header row)</li>
 *   <li>S_prov^2: VAL, k=1, ROW_MAJOR   — airport code in the same row (leftmost cell)</li>
 *   <li>S_prov^3: VAL, k=1, ROW_MAJOR   — month in the same compound cell</li>
 * </ul>
 */
class AtpIllustrativeExampleTest {

    /**
     * Builds the table pattern P_tbl described in Section VI-A of the paper.
     */
    private TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        // P_row^1: skip + one-or-more airline cells
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("AIRLINE"))
                                )
                        ),
                        // P_row^2 (one-or-more): airport cell + one-or-more "ND MON" body cells
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        CompoundContentSpec.of(
                                                AtomicContentSpec.val(
                                                        ActionSpec.rec(1,
                                                                ItemFilterConditionSpec.sameCol(),
                                                                ItemFilterConditionSpec.sameRow(),
                                                                ItemFilterConditionSpec.sameCell()
                                                        ),
                                                        ActionSpec.avp("ND")
                                                ),
                                                CompoundContentSpec.Segment.of(" ",
                                                        AtomicContentSpec.val(ActionSpec.avp("MON"))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static TableSyntax buildTable(String[][] data) {
        var syntax = new TableSyntax(data.length, data[0].length);
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                syntax.getCell(r, c).setText(data[r][c]);
            }
        }
        return syntax;
    }

    /**
     * Matches the pattern against the concrete 3×3 table t₀ from Figure 7 of the paper:
     * <pre>
     *         | CA    | HU
     * IKT     | 0 Jan | 8 Feb
     * SVO     | 31 Jan| 40 Feb
     * </pre>
     * Verifies schema S = ⟨ND, AIRLINE, AIRPORT, MON⟩ and all four extracted records.
     */
    @Test
    void paperExample_3x3_table_t0() {
        var syntax = buildTable(new String[][]{
                {"",    "CA",    "HU"   },
                {"IKT", "0 Jan", "8 Feb"},
                {"SVO", "31 Jan","40 Feb"}
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isPresent(), "Pattern must match table t_0");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(result.get());

        // Schema: ⟨ND, AIRLINE, AIRPORT, MON⟩
        assertEquals(List.of("ND", "AIRLINE", "AIRPORT", "MON"), rs.schema().attributes());

        // Four records corresponding to the four body cells, row-major order
        assertEquals(4, rs.records().size());

        // rec(ι_{1,1}^0): ND=0, AIRLINE=CA, AIRPORT=IKT, MON=Jan
        var r0 = rs.records().get(0);
        assertEquals("0",   r0.get("ND"));
        assertEquals("CA",  r0.get("AIRLINE"));
        assertEquals("IKT", r0.get("AIRPORT"));
        assertEquals("Jan", r0.get("MON"));

        // rec(ι_{1,2}^0): ND=8, AIRLINE=HU, AIRPORT=IKT, MON=Feb
        var r1 = rs.records().get(1);
        assertEquals("8",   r1.get("ND"));
        assertEquals("HU",  r1.get("AIRLINE"));
        assertEquals("IKT", r1.get("AIRPORT"));
        assertEquals("Feb", r1.get("MON"));

        // rec(ι_{2,1}^0): ND=31, AIRLINE=CA, AIRPORT=SVO, MON=Jan
        var r2 = rs.records().get(2);
        assertEquals("31",  r2.get("ND"));
        assertEquals("CA",  r2.get("AIRLINE"));
        assertEquals("SVO", r2.get("AIRPORT"));
        assertEquals("Jan", r2.get("MON"));

        // rec(ι_{2,2}^0): ND=40, AIRLINE=HU, AIRPORT=SVO, MON=Feb
        var r3 = rs.records().get(3);
        assertEquals("40",  r3.get("ND"));
        assertEquals("HU",  r3.get("AIRLINE"));
        assertEquals("SVO", r3.get("AIRPORT"));
        assertEquals("Feb", r3.get("MON"));
    }

    /**
     * Verifies that the same pattern matches a larger 4×5 table (4 airlines, 3 airports),
     * producing 12 records with the correct schema.
     */
    @Test
    void extendedTable_4airlines_3airports() {
        var syntax = buildTable(new String[][]{
                {"",    "SU",     "S7",    "YC",   "U6"   },
                {"ARH", "11 Jan", "0 Jan", "0 Dec","3 Feb" },
                {"IKT", "0 Jan",  "8 Feb", "5 Mar","2 Apr" },
                {"SVO", "31 Jan", "40 Feb","27 Mar","14 Apr"}
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isPresent(), "Pattern must match extended table");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(result.get());

        assertEquals(List.of("ND", "AIRLINE", "AIRPORT", "MON"), rs.schema().attributes());
        // 4 airlines × 3 airports = 12 records
        assertEquals(12, rs.records().size());

        // Spot-check first record: SU from ARH, 11 Jan
        var r0 = rs.records().get(0);
        assertEquals("11",  r0.get("ND"));
        assertEquals("SU",  r0.get("AIRLINE"));
        assertEquals("ARH", r0.get("AIRPORT"));
        assertEquals("Jan", r0.get("MON"));

        // Spot-check last record: U6 from SVO, 14 Apr
        var r11 = rs.records().get(11);
        assertEquals("14",  r11.get("ND"));
        assertEquals("U6",  r11.get("AIRLINE"));
        assertEquals("SVO", r11.get("AIRPORT"));
        assertEquals("Apr", r11.get("MON"));
    }

    /**
     * Verifies that the pattern does not match a table that violates the structure
     * (body cells that do not contain a space-separated pair).
     */
    @Test
    void malformedTable_bodyCell_missingDelimiter_fails() {
        var syntax = buildTable(new String[][]{
                {"",    "CA"  },
                {"IKT", "0"   }  // missing month — compound split must fail
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isEmpty(), "Pattern must not match table with malformed body cell");
    }
}
