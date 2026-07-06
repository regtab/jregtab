package ru.icc.regtab.rtl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code EXT('name')} — named Java bindings in RTL (cell and provider positions). */
class RtlExtBindingsTest {

    // -------- cell match condition position --------

    private static final String GUARDED = """
            [ [EXT('startsTotal') ? VAL] ]
            [ [VAL] ]
            """;

    private static final Bindings GUARD_BINDINGS =
            Bindings.of().cell("startsTotal", c -> c.text().startsWith("Total"));

    @Test
    @DisplayName("EXT cell predicate accepts a matching table")
    void cellPredicateAccepts() {
        TableSyntax syntax = new TableSyntax(2, 1);
        syntax.getCell(0, 0).setText("Total: 5");
        syntax.getCell(1, 0).setText("abc");

        TablePattern p = RtlCompiler.compile(GUARDED, GUARD_BINDINGS);
        assertTrue(AtpMatcher.match(p, syntax).isPresent());
    }

    @Test
    @DisplayName("EXT cell predicate rejects a non-matching table")
    void cellPredicateRejects() {
        TableSyntax syntax = new TableSyntax(2, 1);
        syntax.getCell(0, 0).setText("abc");
        syntax.getCell(1, 0).setText("Total: 5");

        TablePattern p = RtlCompiler.compile(GUARDED, GUARD_BINDINGS);
        assertTrue(AtpMatcher.match(p, syntax).isEmpty());
    }

    // -------- provider constraint position --------

    @Test
    @DisplayName("EXT item filter restricts REC provider items")
    void itemFilterRestrictsProvider() {
        TableSyntax syntax = new TableSyntax(1, 3);
        syntax.getCell(0, 0).setText("A");
        syntax.getCell(0, 1).setText("1");
        syntax.getCell(0, 2).setText("x");

        TablePattern p = RtlCompiler.compile(
                "[ [VAL : (ROW & EXT('isNum'))*->REC] [VAL] [VAL] ]",
                Bindings.of().filter("isNum", (a, c) -> c.str().matches("\\d+")));

        Recordset rs = AtpMatcher.match(p, syntax)
                .map(itm -> new TableInterpreter().interpret(itm))
                .orElseThrow();

        assertEquals(1, rs.size());
        var values = rs.records().get(0).values();
        assertTrue(values.containsValue("A"), "anchor value expected: " + values);
        assertTrue(values.containsValue("1"), "filtered-in value expected: " + values);
        assertFalse(values.containsValue("x"), "filtered-out value present: " + values);
    }

    // -------- serialization --------

    @Test
    @DisplayName("EXT constraints serialize back to EXT('name') and round-trip")
    void serializationRoundTrip() {
        Bindings bindings = Bindings.of()
                .cell("startsTotal", c -> c.text().startsWith("Total"))
                .filter("isNum", (a, c) -> c.str().matches("\\d+"));
        String rtl = """
                [ [EXT('startsTotal') ? VAL : (ROW & EXT('isNum'))*->REC] [VAL] ]
                """;

        TablePattern p = RtlCompiler.compile(rtl, bindings);
        String serialized = AtpToRtlSerializer.serialize(p);
        assertTrue(serialized.contains("EXT('startsTotal')"), serialized);
        assertTrue(serialized.contains("EXT('isNum')"), serialized);

        TablePattern reparsed = RtlCompiler.compile(serialized, bindings);
        assertEquals(serialized, AtpToRtlSerializer.serialize(reparsed));
        assertEquals(p, reparsed);
    }

    // -------- compile-time errors --------

    @Test
    @DisplayName("Unbound EXT cell predicate fails compilation")
    void unboundCellPredicate() {
        var e = assertThrows(RtlCompileException.class,
                () -> RtlCompiler.compile("[ [EXT('nope') ? VAL] ]"));
        assertTrue(e.getMessage().contains("Unbound EXT cell predicate: 'nope'"), e.getMessage());
    }

    @Test
    @DisplayName("Unbound EXT item filter fails compilation")
    void unboundItemFilter() {
        var e = assertThrows(RtlCompileException.class,
                () -> RtlCompiler.compile("[ [VAL : (EXT('nope'))->REC] ]"));
        assertTrue(e.getMessage().contains("Unbound EXT item filter: 'nope'"), e.getMessage());
    }

    @Test
    @DisplayName("Filter binding used in cell position reports a kind hint")
    void wrongKindCellPosition() {
        var e = assertThrows(RtlCompileException.class,
                () -> RtlCompiler.compile("[ [EXT('f') ? VAL] ]",
                        Bindings.of().filter("f", (a, c) -> true)));
        assertTrue(e.getMessage().contains("bound as a filter"), e.getMessage());
    }

    @Test
    @DisplayName("Cell binding used in provider position reports a kind hint")
    void wrongKindProviderPosition() {
        var e = assertThrows(RtlCompileException.class,
                () -> RtlCompiler.compile("[ [VAL : (EXT('g'))->REC] ]",
                        Bindings.of().cell("g", c -> true)));
        assertTrue(e.getMessage().contains("bound as a cell predicate"), e.getMessage());
    }

    @Test
    @DisplayName("Blank EXT name fails compilation")
    void blankName() {
        var e = assertThrows(RtlCompileException.class,
                () -> RtlCompiler.compile("[ [EXT('') ? VAL] ]",
                        Bindings.of().cell("x", c -> true)));
        assertTrue(e.getMessage().contains("must not be blank"), e.getMessage());
    }

    // -------- Bindings API --------

    @Test
    @DisplayName("Bindings rejects duplicates within a kind, allows same name across kinds")
    void bindingsValidation() {
        Bindings b = Bindings.of().cell("n", c -> true);
        assertThrows(IllegalArgumentException.class, () -> b.cell("n", c -> false));
        assertThrows(IllegalArgumentException.class, () -> Bindings.of().cell(" ", c -> true));
        // independent namespaces: same name as cell and as filter is allowed
        Bindings both = b.filter("n", (a, c) -> true);
        assertTrue(both.cellPredicate("n") != null && both.itemFilter("n") != null);
    }
}
