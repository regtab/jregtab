package ru.icc.regtab.itm.rtl;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.atp.AtpMatcher;
import ru.icc.regtab.itm.atp.spec.*;
import ru.icc.regtab.itm.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RtlCompiler}: parsing, structural correctness, and an end-to-end
 * integration test against the illustrative airline/airport example.
 */
class RtlCompilerTest {

    // -------- smoke: parse without throwing --------

    @Test
    void parse_skipCell() {
        TablePattern p = compile("[ [SKIP] ]");
        assertEquals(1, p.subtablePatterns().size());
        var row = row(p, 0, 0);
        var cell = cell(row, 0, 0);
        assertNull(cell.contentSpec(), "SKIP must produce null contentSpec");
        assertEquals(Quantifier.one(), cell.quantifier());
    }

    @Test
    void parse_valCell_noActions() {
        TablePattern p = compile("[ [VAL] ]");
        var cell = cell(row(p, 0, 0), 0, 0);
        var cs = assertAtom(cell);
        assertEquals(ItemDerivationDirective.VAL, cs.idd());
        assertTrue(cs.actions().isEmpty());
    }

    @Test
    void parse_attrCell() {
        TablePattern p = compile("[ [ATTR] ]");
        assertEquals(ItemDerivationDirective.ATTR, assertAtom(cell(row(p, 0, 0), 0, 0)).idd());
    }

    @Test
    void parse_auxCell() {
        TablePattern p = compile("[ [AUX] ]");
        assertEquals(ItemDerivationDirective.AUX, assertAtom(cell(row(p, 0, 0), 0, 0)).idd());
    }

    @Test
    void parse_quantifiers() {
        TablePattern p = compile("[ [SKIP]? [SKIP]+ [SKIP]* [SKIP]{3} ]");
        var row = row(p, 0, 0);
        assertEquals(Quantifier.zeroOrOne(),  cell(row, 0, 0).quantifier());
        assertEquals(Quantifier.oneOrMore(),  cell(row, 0, 1).quantifier());
        assertEquals(Quantifier.zeroOrMore(), cell(row, 0, 2).quantifier());
        assertEquals(Quantifier.exactly(3),   cell(row, 0, 3).quantifier());
    }

    @Test
    void parse_ctxProviderAvp() {
        TablePattern p = compile("[ [VAL : ('LABEL')->AVP] ]");
        var actions = assertAtom(cell(row(p, 0, 0), 0, 0)).actions();
        assertEquals(1, actions.size());
        var a = actions.get(0);
        assertEquals(OperationType.AVP, a.operationType());
        assertEquals(1, a.providers().size());
        assertTrue(a.providers().get(0).isContextLiteral());
        assertEquals("LABEL", a.providers().get(0).contextLiteral().text());
    }

    @Test
    void parse_tblProviderRec_withCardinality() {
        TablePattern p = compile("[ [VAL : (UW{1})->REC] ]");
        var a = assertAtom(cell(row(p, 0, 0), 0, 0)).actions().get(0);
        assertEquals(OperationType.REC, a.operationType());
        var ps = a.providers().get(0);
        assertFalse(ps.isContextLiteral());
        assertEquals(1, ps.cardinality());
        assertEquals(TraversalOrder.REVERSE_COLUMN_MAJOR, ps.traversalOrder());
    }

    @Test
    void parse_allProviderTemplates() {
        compile("""
                [ [VAL : (LW)->REC] ]
                [ [VAL : (RW)->REC] ]
                [ [VAL : (UW)->REC] ]
                [ [VAL : (DW)->REC] ]
                [ [VAL : (RM)->REC] ]
                [ [VAL : (CM)->REC] ]
                [ [VAL : (CL)->REC] ]
                """);
    }

    @Test
    void parse_spatialConstraints() {
        compile("[ [VAL : (DW(COL+0))->REC] ]");
        compile("[ [VAL : (DW(ROW-1))->REC] ]");
        compile("[ [VAL : (CL(POS0..5))->REC] ]");
        compile("[ [VAL : (RM(COL+0..-1))->REC] ]");
    }

    @Test
    void parse_contentConstraints() {
        compile("[ [VAL : (RM(\"pattern\"))->REC] ]");
        compile("[ [VAL : (RM(BLANK))->REC] ]");
        compile("[ [VAL : (RM(TAG #t1 #t2))->REC] ]");
    }

    @Test
    void parse_multipleProviders() {
        TablePattern p = compile("[ [VAL : (UW{1}, LW{1}, CL{1})->REC] ]");
        var a = assertAtom(cell(row(p, 0, 0), 0, 0)).actions().get(0);
        assertEquals(OperationType.REC, a.operationType());
        assertEquals(3, a.providers().size());
    }

    @Test
    void parse_tags() {
        TablePattern p = compile("[ [VAL #head #bold] ]");
        var cs = assertAtom(cell(row(p, 0, 0), 0, 0));
        assertEquals(List.of("#head", "#bold"), cs.tags());
    }

    @Test
    void parse_stringExtractors() {
        compile("[ [VAL = UC] ]");
        compile("[ [VAL = LC] ]");
        compile("[ [VAL = SUBSTR(0, 3)] ]");
        compile("[ [VAL = REPL('a', 'b')] ]");
    }

    @Test
    void parse_fillPrefixSuffixOps() {
        compile("[ [VAL : (CL)->FILL] ]");
        compile("[ [VAL : (CL)->FILL('/')] ]");
        compile("[ [VAL : (CL)->PREFIX(' ')] ]");
        compile("[ [VAL : (CL)->SUFFIX(',')] ]");
        compile("[ [VAL : (CL)->CONCAT] ]");
    }

    @Test
    void parse_cellMatchCondition_regex() {
        TablePattern p = compile("[ [\"^\\\\d+$\"? VAL] ]");
        var cell = cell(row(p, 0, 0), 0, 0);
        assertNotNull(cell.condition());
    }

    @Test
    void parse_cellMatchCondition_blank() {
        TablePattern p = compile("[ [BLANK? VAL] ]");
        assertNotNull(cell(row(p, 0, 0), 0, 0).condition());
    }

    @Test
    void parse_delimitedContentSpec() {
        compile("[ [(VAL : ('X')->AVP){','}] ]");
    }

    @Test
    void parse_compoundContentSpec() {
        compile("[ [VAL : ('ND')->AVP ' ' VAL : ('MON')->AVP] ]");
    }

    @Test
    void parse_conditionalContentSpec() {
        compile("[ [(BLANK? SKIP | VAL : ('X')->AVP)] ]");
    }

    @Test
    void parse_explicitSubrowPattern() {
        compile("[ { [SKIP]+ }* ]");
    }

    @Test
    void parse_explicitSubtablePattern() {
        compile("{ [ [SKIP] ] }+");
    }

    @Test
    void parse_rowQuantifier() {
        TablePattern p = compile("[ [SKIP] ]+");
        assertEquals(Quantifier.oneOrMore(), p.subtablePatterns().get(0).rowPatterns().get(0).quantifier());
    }

    @Test
    void parse_multipleRows() {
        TablePattern p = compile("""
                [ [SKIP] ]
                [ [VAL] ]+
                """);
        var st = p.subtablePatterns().get(0);
        assertEquals(2, st.rowPatterns().size());
        assertEquals(Quantifier.one(),       st.rowPatterns().get(0).quantifier());
        assertEquals(Quantifier.oneOrMore(), st.rowPatterns().get(1).quantifier());
    }

    @Test
    void parse_invalidRtl_throwsRtlCompileException() {
        assertThrows(RtlCompileException.class, () -> compile("[ [INVALID_TOKEN] ]"));
        assertThrows(RtlCompileException.class, () -> compile(""));
    }

    // -------- structural correctness --------

    @Test
    void structure_simpleSkipPlusVal() {
        TablePattern p = compile("[ [SKIP] [VAL]+]");
        var st = p.subtablePatterns().get(0);
        assertEquals(1, st.rowPatterns().size());

        var row = row(p, 0, 0);
        assertEquals(Quantifier.one(), row.quantifier());
        assertEquals(1, row.subrowPatterns().size());

        var cells = row.subrowPatterns().get(0).cellPatterns();
        assertEquals(2, cells.size());

        assertNull(cells.get(0).contentSpec(), "first cell is SKIP");
        assertEquals(Quantifier.one(), cells.get(0).quantifier());
        assertEquals(Quantifier.oneOrMore(), cells.get(1).quantifier());
        assertEquals(ItemDerivationDirective.VAL, assertAtom(cells.get(1)).idd());
    }

    @Test
    void structure_providerTraversalOrders() {
        String rtl = """
                [ [VAL : (LW)->REC] ]
                [ [VAL : (RW)->REC] ]
                [ [VAL : (UW)->REC] ]
                [ [VAL : (DW)->REC] ]
                [ [VAL : (RM)->REC] ]
                [ [VAL : (CM)->REC] ]
                [ [VAL : (CL)->REC] ]
                """;
        TablePattern p = compile(rtl);
        TraversalOrder[] expected = {
                TraversalOrder.REVERSE_ROW_MAJOR,
                TraversalOrder.ROW_MAJOR,
                TraversalOrder.REVERSE_COLUMN_MAJOR,
                TraversalOrder.COLUMN_MAJOR,
                TraversalOrder.ROW_MAJOR,
                TraversalOrder.COLUMN_MAJOR,
                TraversalOrder.ROW_MAJOR
        };
        for (int i = 0; i < 7; i++) {
            var a = assertAtom(cell(row(p, 0, i), 0, 0)).actions().get(0);
            assertEquals(expected[i], a.providers().get(0).traversalOrder(),
                    "Wrong traversal order for row " + i);
        }
    }

    // -------- integration test: airline/airport illustrative example --------

    /**
     * Replicates the illustrative example from AtpIllustrativeExampleTest using an RTL string.
     * Providers are UNRESTRICTED (Phase 1); behavior is identical because all items are VAL type.
     * <p>
     * RTL equivalent of the hand-coded Java pattern:
     * <pre>
     *   [ [SKIP] [VAL : ('AIRLINE')->AVP]+ ]
     *   [ [VAL : ('AIRPORT')->AVP]
     *     [VAL : (UW{1}, LW{1}, CL{1})->REC, ('ND')->AVP " " VAL : ('MON')->AVP]+ ]+
     * </pre>
     *
     * Providers per ND item:
     * <ul>
     *   <li>UW{1}: val item above in same column → airline code from header</li>
     *   <li>LW{1}: val item to the left in same row → airport code</li>
     *   <li>CL{1}: val item in same cell (anchor excluded) → month</li>
     * </ul>
     */
    @Test
    void integration_illustrativeExample_3x3() {
        TablePattern p = compile("""
                [ [SKIP] [VAL : ('AIRLINE')->AVP]+ ]
                [ [VAL : ('AIRPORT')->AVP]
                  [VAL : (CM{1}, LW{1}, CL{1})->REC, ('ND')->AVP " " VAL : ('MON')->AVP]+ ]+
                """);

        var syntax = buildTable(new String[][]{
                {"",    "CA",    "HU"   },
                {"IKT", "0 Jan", "8 Feb"},
                {"SVO", "31 Jan","40 Feb"}
        });

        Optional<InterpretableTable> result = AtpMatcher.match(p, syntax);
        assertTrue(result.isPresent(), "Pattern must match");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(result.get());

        assertEquals(List.of("ND", "AIRLINE", "AIRPORT", "MON"), rs.schema().attributes());
        assertEquals(4, rs.records().size());

        var r0 = rs.records().get(0);
        assertEquals("0",   r0.get("ND"));
        assertEquals("CA",  r0.get("AIRLINE"));
        assertEquals("IKT", r0.get("AIRPORT"));
        assertEquals("Jan", r0.get("MON"));

        var r1 = rs.records().get(1);
        assertEquals("8",   r1.get("ND"));
        assertEquals("HU",  r1.get("AIRLINE"));
        assertEquals("IKT", r1.get("AIRPORT"));
        assertEquals("Feb", r1.get("MON"));

        var r2 = rs.records().get(2);
        assertEquals("31",  r2.get("ND"));
        assertEquals("CA",  r2.get("AIRLINE"));
        assertEquals("SVO", r2.get("AIRPORT"));
        assertEquals("Jan", r2.get("MON"));

        var r3 = rs.records().get(3);
        assertEquals("40",  r3.get("ND"));
        assertEquals("HU",  r3.get("AIRLINE"));
        assertEquals("SVO", r3.get("AIRPORT"));
        assertEquals("Feb", r3.get("MON"));
    }

    // -------- helpers --------

    private static TablePattern compile(String rtl) {
        return RtlCompiler.compile(rtl);
    }

    private static RowPattern row(TablePattern p, int subtable, int row) {
        return p.subtablePatterns().get(subtable).rowPatterns().get(row);
    }

    private static CellPattern cell(RowPattern row, int subrow, int cell) {
        return row.subrowPatterns().get(subrow).cellPatterns().get(cell);
    }

    private static AtomicContentSpec assertAtom(CellPattern cell) {
        assertNotNull(cell.contentSpec(), "Expected non-null contentSpec");
        assertInstanceOf(AtomicContentSpec.class, cell.contentSpec());
        return (AtomicContentSpec) cell.contentSpec();
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
}
