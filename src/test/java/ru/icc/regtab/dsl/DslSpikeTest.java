package ru.icc.regtab.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.rtl.AtpToRtlSerializer;
import ru.icc.regtab.rtl.RtlCompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.icc.regtab.dsl.Rtl.ATTR;
import static ru.icc.regtab.dsl.Rtl.AUX;
import static ru.icc.regtab.dsl.Rtl.BW;
import static ru.icc.regtab.dsl.Rtl.C;
import static ru.icc.regtab.dsl.Rtl.CL;
import static ru.icc.regtab.dsl.Rtl.COL;
import static ru.icc.regtab.dsl.Rtl.NORM;
import static ru.icc.regtab.dsl.Rtl.ROW;
import static ru.icc.regtab.dsl.Rtl.RT;
import static ru.icc.regtab.dsl.Rtl.SC;
import static ru.icc.regtab.dsl.Rtl.SKIP;
import static ru.icc.regtab.dsl.Rtl.SR;
import static ru.icc.regtab.dsl.Rtl.ST;
import static ru.icc.regtab.dsl.Rtl.STR;
import static ru.icc.regtab.dsl.Rtl.VAL;
import static ru.icc.regtab.dsl.Rtl.avp;
import static ru.icc.regtab.dsl.Rtl.blank;
import static ru.icc.regtab.dsl.Rtl.cell;
import static ru.icc.regtab.dsl.Rtl.ctxAvp;
import static ru.icc.regtab.dsl.Rtl.join;
import static ru.icc.regtab.dsl.Rtl.notBlank;
import static ru.icc.regtab.dsl.Rtl.rec;
import static ru.icc.regtab.dsl.Rtl.row;
import static ru.icc.regtab.dsl.Rtl.skip;
import static ru.icc.regtab.dsl.Rtl.sub;
import static ru.icc.regtab.dsl.Rtl.suffix;
import static ru.icc.regtab.dsl.Rtl.table;
import static ru.icc.regtab.dsl.Rtl.tag;
import static ru.icc.regtab.dsl.Rtl.val;
import static ru.icc.regtab.dsl.Rtl.when;

/**
 * Design spike B1 (plans/RTL_EMBEDDED_DSL.md): embedded RTL must build byte-identical
 * ATP for 10 representative tasks. Each test shows the RTL source next to its DSL mirror
 * and asserts full structural equality with the compiled pattern.
 */
class DslSpikeTest {

    private static void assertMirrors(String rtl, TablePattern dsl) {
        TablePattern compiled = RtlCompiler.compile(rtl);
        assertEquals(AtpToRtlSerializer.serialize(compiled), AtpToRtlSerializer.serialize(dsl));
        assertEquals(compiled, dsl);
    }

    @Test
    @DisplayName("001: explicit subtable, unbounded REC, exact and one-or-more quantifiers")
    void task001() {
        assertMirrors("""
                { [ [VAL : ST*->REC] [VAL]{2} []+ ]
                [ [] [VAL]{4} []+ ] }+
                """,
                table(
                        sub(
                                row(cell(VAL, rec(ST.unbounded())), cell(VAL).exactly(2), skip().oneOrMore()),
                                row(skip(), cell(VAL).exactly(4), skip().oneOrMore())
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("002: NORM extractor, guard, provider cardinality, REC(n), optional row")
    void task002() {
        assertMirrors("""
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
                  [ [BLANK] [] ]? }+
                """,
                table(
                        sub(
                                row(cell(val().extract(NORM)), skip()).exactly(2),
                                row(cell(notBlank(), VAL, rec(2, SC.card(2), SR)), cell(VAL)).oneOrMore(),
                                row(cell(blank()), skip()).zeroOrOne()
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("006: conditional content spec BLANK ? _ | VAL")
    void task006() {
        assertMirrors("""
                { [ [VAL : ST*->REC] [BLANK ? _ | VAL]+ ]
                  [ [BLANK ? _ | VAL]+ ]{4} }+
                """,
                table(
                        sub(
                                row(cell(VAL, rec(ST.unbounded())), cell(when(blank(), SKIP, VAL)).oneOrMore()),
                                row(cell(when(blank(), SKIP, VAL)).oneOrMore()).exactly(4)
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("015: compound cell with three delimited segments and REC(1)")
    void task015() {
        assertMirrors("""
                [ [VAL ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1)] ]+
                """,
                table(sub(
                        row(cell(val()
                                .then(" ", val(rec(1, CL)))
                                .then(" ", val(rec(1, CL)))
                                .then(" ", val(rec(1, CL))))
                        ).oneOrMore())));
    }

    @Test
    @DisplayName("016: REC + JOIN(0) with bare conjunction BW&STR*")
    void task016() {
        assertMirrors("""
                [ [VAL : RT->REC, BW&STR*->JOIN(0)] [VAL] ]+
                """,
                table(sub(
                        row(cell(VAL, rec(RT), join(0, BW.and(STR).unbounded())), cell(VAL))
                                .oneOrMore())));
    }

    @Test
    @DisplayName("022: column-major traversal, absolute column range ^ST&C2..5*")
    void task022() {
        assertMirrors("""
                { [ [VAL : ^ST&C2..5*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
                """,
                table(
                        sub(
                                row(cell(VAL, rec(ST.and(C(2, 5)).unbounded().colMajor())),
                                        skip(), cell(VAL).oneOrMore()),
                                row(skip().exactly(2), cell(VAL).oneOrMore())
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("023: empty context AVP, SUFFIX, AUX, provider-based AVP")
    void task023() {
        assertMirrors("""
                { [ [VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)] [ATTR : RT->SUFFIX] [AUX] [VAL : SR->AVP] ]{3} }+
                """,
                table(
                        sub(
                                row(cell(VAL, avp(""), rec(SR.unbounded()), join(0, BW.and(STR).unbounded())),
                                        cell(ATTR, suffix(RT)),
                                        cell(AUX),
                                        cell(VAL, avp(SR))
                                ).exactly(3)
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("045: guards and a delimited cell (VAL : SR&C0->REC(1)){','}")
    void task045() {
        assertMirrors("""
                [ [!BLANK? VAL] [!BLANK? (VAL : SR&C0->REC(1)){','}] ]+
                """,
                table(sub(
                        row(cell(notBlank(), VAL),
                                cell(notBlank(), val(rec(1, SR.and(C(0)))).splitBy(",")))
                                .oneOrMore())));
    }

    @Test
    @DisplayName("052: compound with context providers 'ND'->AVP and @'YEAR'='2025'")
    void task052() {
        assertMirrors("""
                [ [] [VAL : 'AIRLINE'->AVP]+ ]
                [ [VAL : 'AIRPORT'->AVP]
                  [VAL : (COL, ROW, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
                """,
                table(sub(
                        row(skip(), cell(VAL, avp("AIRLINE")).oneOrMore()),
                        row(cell(VAL, avp("AIRPORT")),
                                cell(val(rec(COL, ROW, CL, ctxAvp("YEAR", "2025")), avp("ND"))
                                        .then(" ", val(avp("MON"))))
                                        .oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("068: tags on atoms and in provider constraints COL&#'HEAD'*")
    void task068() {
        assertMirrors("""
                [ [BLANK] [VAL #'HEAD']+ ]+
                [ [!BLANK? VAL] [VAL: (COL&#'HEAD'*, ROW)->REC]+ ]+
                """,
                table(sub(
                        row(cell(blank()), cell(val().tagged("HEAD")).oneOrMore()).oneOrMore(),
                        row(cell(notBlank(), VAL),
                                cell(VAL, rec(COL.and(tag("HEAD")).unbounded(), ROW)).oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("Escape hatch: .where() on a provider builds a Custom filter term")
    void escapeHatch() {
        // No RTL equivalent by design — just verify it builds a valid pattern.
        TablePattern p = table(sub(row(
                cell(VAL, rec(ROW.where("isNum", (a, c) -> c.str().matches("\\d+")).unbounded())),
                cell(VAL).oneOrMore())));
        assertEquals(1, p.subtablePatterns().size());
    }
}
