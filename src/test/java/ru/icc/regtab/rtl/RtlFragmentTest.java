package ru.icc.regtab.rtl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that named fragment definitions ($name=[body]) expand to the same
 * TablePattern as the equivalent inline form, for all four pattern levels.
 * Each test compiles a fragment form and its expanded equivalent, then asserts
 * structural equality (records have automatic equals based on components).
 */
class RtlFragmentTest {

    private static Object compile(String rtl) {
        return RtlCompiler.compile(rtl);
    }

    // -------- cell-level fragment [$N] --------

    @Test
    void cellFragment() {
        var withFrag = compile("""
                $C=[VAL]
                [ [$C]{2} ]
                """);
        var expanded = compile("""
                [ [VAL]{2} ]
                """);
        assertEquals(expanded, withFrag);
    }

    @Test
    void cellFragmentWithBody() {
        var withFrag = compile("""
                $C=[VAL: 'X'->AVP]
                [ [$C] [$C] ]
                """);
        var expanded = compile("""
                [ [VAL: 'X'->AVP] [VAL: 'X'->AVP] ]
                """);
        assertEquals(expanded, withFrag);
    }

    // -------- row-level fragment [$N] --------

    @Test
    void rowFragment() {
        var withFrag = compile("""
                $R=[ [VAL] [AUX] ]
                { [$R]+ }+
                """);
        var expanded = compile("""
                { [ [VAL] [AUX] ]+ }+
                """);
        assertEquals(expanded, withFrag);
    }

    @Test
    void rowFragmentWithQuantifier() {
        var withFrag = compile("""
                $R=[ [VAL: 'Y'->AVP] [] ]
                [$R]{3}
                """);
        var expanded = compile("""
                [ [VAL: 'Y'->AVP] [] ]{3}
                """);
        assertEquals(expanded, withFrag);
    }

    // -------- subrow-level fragment {$N} --------

    @Test
    void subrowFragment() {
        var withFrag = compile("""
                $SR={ [VAL] [AUX] }
                [ {$SR}+ ]
                """);
        var expanded = compile("""
                [ { [VAL] [AUX] }+ ]
                """);
        assertEquals(expanded, withFrag);
    }

    @Test
    void subrowFragmentWithQuantifier() {
        var withFrag = compile("""
                $SR={ [VAL] [] }
                [ {$SR}{2} {$SR}? ]
                """);
        var expanded = compile("""
                [ { [VAL] [] }{2} { [VAL] [] }? ]
                """);
        assertEquals(expanded, withFrag);
    }

    // -------- subtable-level fragment {$N} --------

    @Test
    void subtableFragment() {
        var withFrag = compile("""
                $ST={ [ [VAL] [AUX] ] }
                {$ST}+
                """);
        var expanded = compile("""
                { [ [VAL] [AUX] ] }+
                """);
        assertEquals(expanded, withFrag);
    }

    @Test
    void subtableFragmentWithQuantifier() {
        var withFrag = compile("""
                $ST={ [ [VAL: 'Z'->AVP] [] ] }
                {$ST}{2}
                """);
        var expanded = compile("""
                { [ [VAL: 'Z'->AVP] [] ] }{2}
                """);
        assertEquals(expanded, withFrag);
    }

    // -------- multiple fragments in one pattern --------

    @Test
    void multipleFragments() {
        var withFrag = compile("""
                $C=[VAL: 'A'->AVP]
                $R=[ [$C] [] ]
                [$R]{2}
                """);
        var expanded = compile("""
                [ [VAL: 'A'->AVP] [] ]{2}
                """);
        assertEquals(expanded, withFrag);
    }

    // -------- unknown fragment → RtlCompileException --------

    @Test
    void unknownCellFragmentThrows() {
        assertThrows(RtlCompileException.class,
                () -> compile("[ [$UNKNOWN] ]"));
    }

    @Test
    void unknownRowFragmentThrows() {
        assertThrows(RtlCompileException.class,
                () -> compile("{ [$UNKNOWN] }+"));
    }

    @Test
    void unknownSubrowFragmentThrows() {
        assertThrows(RtlCompileException.class,
                () -> compile("[ {$UNKNOWN} ]"));
    }

    @Test
    void unknownSubtableFragmentThrows() {
        assertThrows(RtlCompileException.class,
                () -> compile("{$UNKNOWN}+"));
    }
}
