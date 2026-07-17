package ru.icc.regtab.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.rtl.AtpToRtlSerializer;
import ru.icc.regtab.rtl.RtlCompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.icc.regtab.dsl.Rtl.*;

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
        assertMirrors(/* language=RTL */ """
                { [ [VAL : ST*->REC] [VAL]{2} []+ ]
                [ [] [VAL]{4} []+ ] }+
                """,
                table(
                        subtable(
                                row(cell(VAL, rec(ST.unbounded())), cell(VAL).exactly(2), skip().oneOrMore()),
                                row(skip(), cell(VAL).exactly(4), skip().oneOrMore())
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("002: NORM extractor, guard, provider cardinality, REC(n), optional row")
    void task002() {
        assertMirrors(/* language=RTL */ """
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
                  [ [BLANK] [] ]? }+
                """,
                table(
                        subtable(
                                row(cell(val().extract(NORM)), skip()).exactly(2),
                                row(cell(notBlank(), VAL, rec(2, SC.card(2), SR)), cell(VAL)).oneOrMore(),
                                row(cell(blank()), skip()).zeroOrOne()
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("006: conditional content spec BLANK ? _ | VAL")
    void task006() {
        assertMirrors(/* language=RTL */ """
                { [ [VAL : ST*->REC] [BLANK ? _ | VAL]+ ]
                  [ [BLANK ? _ | VAL]+ ]{4} }+
                """,
                table(
                        subtable(
                                row(cell(VAL, rec(ST.unbounded())), cell(when(blank(), SKIP, VAL)).oneOrMore()),
                                row(cell(when(blank(), SKIP, VAL)).oneOrMore()).exactly(4)
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("015: compound cell with three delimited segments and REC(1)")
    void task015() {
        assertMirrors(/* language=RTL */ """
                [ [VAL ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1) ' ' VAL : CL->REC(1)] ]+
                """,
                table(subtable(
                        row(cell(val()
                                .then(" ", val(rec(1, CL)))
                                .then(" ", val(rec(1, CL)))
                                .then(" ", val(rec(1, CL))))
                        ).oneOrMore())));
    }

    @Test
    @DisplayName("016: REC + JOIN(0) with bare conjunction BW&STR*")
    void task016() {
        assertMirrors(/* language=RTL */ """
                [ [VAL : RT->REC, BW&STR*->JOIN(0)] [VAL] ]+
                """,
                table(subtable(
                        row(cell(VAL, rec(RT), join(0, BW.and(STR).unbounded())), cell(VAL))
                                .oneOrMore())));
    }

    @Test
    @DisplayName("022: column-major traversal, absolute column range ^ST&C2..5*")
    void task022() {
        assertMirrors(/* language=RTL */ """
                { [ [VAL : ^ST&C2..5*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
                """,
                table(
                        subtable(
                                row(cell(VAL, rec(ST.and(C(2, 5)).unbounded().colMajor())),
                                        skip(), cell(VAL).oneOrMore()),
                                row(skip().exactly(2), cell(VAL).oneOrMore())
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("023: empty context AVP, SUFFIX, AUX, provider-based AVP")
    void task023() {
        assertMirrors(/* language=RTL */ """
                { [ [VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)] [ATTR : RT->SUFFIX] [AUX] [VAL : SR->AVP] ]{3} }+
                """,
                table(
                        subtable(
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
        assertMirrors(/* language=RTL */ """
                [ [!BLANK? VAL] [!BLANK? (VAL : SR&C0->REC(1)){','}] ]+
                """,
                table(subtable(
                        row(cell(notBlank(), VAL),
                                cell(notBlank(), val(rec(1, SR.and(C(0)))).splitBy(",")))
                                .oneOrMore())));
    }

    @Test
    @DisplayName("052: compound with context providers 'ND'->AVP and @'YEAR'='2025'")
    void task052() {
        assertMirrors(/* language=RTL */ """
                [ [] [VAL : 'AIRLINE'->AVP]+ ]
                [ [VAL : 'AIRPORT'->AVP]
                  [VAL : (COL, ROW, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
                """,
                table(subtable(
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
        assertMirrors(/* language=RTL */ """
                [ [BLANK] [VAL #'HEAD']+ ]+
                [ [!BLANK? VAL] [VAL: (COL&#'HEAD'*, ROW)->REC]+ ]+
                """,
                table(subtable(
                        row(cell(blank()), cell(val().tagged("HEAD")).oneOrMore()).oneOrMore(),
                        row(cell(notBlank(), VAL),
                                cell(VAL, rec(COL.and(tag("HEAD")).unbounded(), ROW)).oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("009: REPL extractor, explicit subrow, conditional with actions in a branch")
    void task009() {
        assertMirrors(/* language=RTL */ """
                [ [] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [BLANK? _ | VAL : (SR, SC)->REC(2)]+ } ]+
                """,
                table(subtable(
                        row(skip(), cell(val().extract(repl("\\s+", ""))).exactly(5)),
                        row(subrow(cell(VAL),
                                cell(when(blank(), SKIP, val(rec(2, SR, SC)))).oneOrMore()))
                                .oneOrMore())));
    }

    @Test
    @DisplayName("013: AVP per column plus REC with explicitly ordered SR&Cn providers")
    void task013() {
        assertMirrors(/* language=RTL */ """
                [ [ATTR]{5} []+ ]
                [ [VAL : SC->AVP, (SR&C2, SR&C4, SR&C1, SR&C3)->REC] [VAL : SC->AVP]{4} []+ ]+
                """,
                table(subtable(
                        row(cell(ATTR).exactly(5), skip().oneOrMore()),
                        row(cell(VAL, avp(SC), rec(SR.and(C(2)), SR.and(C(4)), SR.and(C(1)), SR.and(C(3)))),
                                cell(VAL, avp(SC)).exactly(4), skip().oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("025: SUFFIX('/'), REC('/') split, relative open column range C+2..*")
    void task025() {
        assertMirrors(/* language=RTL */ """
                [ [VAL : RT->SUFFIX('/'), RT&C+2..*->REC('/'), BW&STR*->JOIN(0)] [VAL]+ ]+
                """,
                table(subtable(row(
                        cell(VAL, suffix("/", RT),
                                recSplit("/", RT.and(CrelFrom(2)).unbounded()),
                                join(0, BW.and(STR).unbounded())),
                        cell(VAL).oneOrMore()).oneOrMore())));
    }

    @Test
    @DisplayName("029: implicit + explicit subrows mixed in one row, ROW{6} provider")
    void task029() {
        assertMirrors(/* language=RTL */ """
                [ [VAL]{6} { [VAL : (ROW{6}, RT*)->REC(6)] [VAL]{3} }+ ]+
                """,
                table(subtable(row(
                        subrow(cell(VAL).exactly(6)),
                        subrow(cell(VAL, rec(6, ROW.card(6), RT.unbounded())),
                                cell(VAL).exactly(3)).oneOrMore())
                        .oneOrMore())));
    }

    @Test
    @DisplayName("069: row-level inherited REC merged down into subrow atoms")
    void task069() {
        assertMirrors(/* language=RTL */ """
                [ BW*->REC { [ATTR] [VAL#'1': ROW&#'1'*->JOIN][VAL#'2': ROW&#'2'*->JOIN] }* ]
                """,
                table(subtable(row(acts(rec(BW.unbounded())),
                        subrow(cell(ATTR),
                                cell(val(join(ROW.and(tag("1")).unbounded())).tagged("1")),
                                cell(val(join(ROW.and(tag("2")).unbounded())).tagged("2")))
                                .zeroOrMore()))));
    }

    @Test
    @DisplayName("070: regex guards with tags on atoms and in constraints")
    void task070() {
        assertMirrors(/* language=RTL */ """
                [ [BLANK]+           [VAL#'H']+ ]+
                [ [!'\\d+'? VAL#'S']+ ['\\d+'? VAL: (COL&#'H'*, ROW&#'S'*)->REC]+ ]+
                """,
                table(subtable(
                        row(cell(blank()).oneOrMore(), cell(val().tagged("H")).oneOrMore()).oneOrMore(),
                        row(cell(notRe("\\d+"), val().tagged("S")).oneOrMore(),
                                cell(re("\\d+"), VAL,
                                        rec(COL.and(tag("H")).unbounded(), ROW.and(tag("S")).unbounded()))
                                        .oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("071: SUFFIX('/') on tagged atoms in both header and stub rows")
    void task071() {
        assertMirrors(/* language=RTL */ """
                [ [BLANK]+       [VAL#'H': BW&#'H'*->SUFFIX('/')]+ ]+
                [ [!'\\d+'? VAL#'S': RT&#'S'*->SUFFIX('/')]+ ['\\d+'? VAL: (COL, ROW)->REC]+ ]+
                """,
                table(subtable(
                        row(cell(blank()).oneOrMore(),
                                cell(val(suffix("/", BW.and(tag("H")).unbounded())).tagged("H")).oneOrMore())
                                .oneOrMore(),
                        row(cell(notRe("\\d+"),
                                        val(suffix("/", RT.and(tag("S")).unbounded())).tagged("S")).oneOrMore(),
                                cell(re("\\d+"), VAL, rec(COL, ROW)).oneOrMore())
                                .oneOrMore())));
    }

    @Test
    @DisplayName("074: row-level inherited COL->AVP plus @'D'='d' context pair")
    void task074() {
        assertMirrors(/* language=RTL */ """
                [ COL->AVP [VAL: (RT*, @'D'='d')->REC][VAL]{2} ]+
                """,
                table(subtable(row(acts(avp(COL)),
                        cell(VAL, rec(RT.unbounded(), ctxAvp("D", "d"))),
                        cell(VAL).exactly(2)).oneOrMore())));
    }

    @Test
    @DisplayName("107: fragments as variables, FILL with reversed traversal, conditional branch actions")
    void task107() {
        var v = cell(re("\\d+"), VAL,
                rec(COL.and(tag("H")).unbounded(), ROW.and(tag("S")).unbounded()));
        assertMirrors(/* language=RTL */ """
                $V=['\\d+' ? VAL: (COL&#'H'*,ROW&#'S'*)->REC]
                [ [BLANK]+ [!BLANK ? VAL#'H'] [BLANK ? VAL#'H': -LT&!BLANK->FILL | VAL#'H']+ ]+
                {
                [ ['\\D.*' ? VAL#'S']+ [$V]+ ]
                [ [BLANK ? VAL#'S': SC->FILL]+ ['\\D.*' ? VAL#'S']+ [$V]+ ]*
                }+
                """,
                table(
                        subtable(row(cell(blank()).oneOrMore(),
                                cell(notBlank(), val().tagged("H")),
                                cell(when(blank(),
                                        val(fill(LT.and(itemNotBlank()).reversed())).tagged("H"),
                                        val().tagged("H"))).oneOrMore())
                                .oneOrMore()),
                        subtable(
                                row(cell(re("\\D.*"), val().tagged("S")).oneOrMore(), v.oneOrMore()),
                                row(cell(blank(), val(fill(SC)).tagged("S")).oneOrMore(),
                                        cell(re("\\D.*"), val().tagged("S")).oneOrMore(),
                                        v.oneOrMore()).zeroOrMore()
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("116: fragments, reversed PREFIX, row-level AVP, nested explicit subrows")
    void task116() {
        var v1 = cell(VAL, prefix(", ", AV.reversed()));
        var v2 = cell(VAL, avp("VALUE"),
                rec(ROW, COL.and(R(1, 3)).unbounded(), AV.and(tag("IND")).reversed()));
        assertMirrors(/* language=RTL */ """
                $V1=[VAL: -AV->PREFIX(', ')]
                $V2=[VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]
                [ []+ ]
                [ [] [VAL: 'TERRITORY'->AVP]+ ]
                [ [AUX]+ ]
                [ 'LOCATION'->AVP [] [$V1]{4} [VAL] []
                                     [VAL] [$V1] [VAL]
                                     [$V1] [VAL] []
                                     { [VAL] [$V1] [VAL] [] }? ]
                { [ [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+ ]
                  [ ['20\\d\\d' ? VAL: 'YEAR'->AVP]
                    { [$V2]{5} [] }{2}
                    { [$V2]{3} [] }?
                  ]+
                }+
                """,
                table(
                        subtable(row(skip().oneOrMore()),
                                row(skip(), cell(VAL, avp("TERRITORY")).oneOrMore()),
                                row(cell(AUX).oneOrMore()),
                                row(acts(avp("LOCATION")),
                                        subrow(skip(), v1.exactly(4), cell(VAL), skip(),
                                                cell(VAL), v1, cell(VAL),
                                                v1, cell(VAL), skip()),
                                        subrow(cell(VAL), v1, cell(VAL), skip()).zeroOrOne())),
                        subtable(
                                row(cell(val(avp("INDICATOR")).tagged("IND")
                                        .then(",", val(avp("UNIT")))).oneOrMore()),
                                row(subrow(cell(re("20\\d\\d"), VAL, avp("YEAR"))),
                                        subrow(v2.exactly(5), skip()).exactly(2),
                                        subrow(v2.exactly(3), skip()).zeroOrOne())
                                        .oneOrMore()
                        ).oneOrMore()));
    }

    @Test
    @DisplayName("Ad-hoc: OR disjunction with & precedence (SR&#'t1'|#'t2')")
    void adhocOrDisjunction() {
        assertMirrors(/* language=RTL */ """
                [ [VAL : (SR&#'t1'|#'t2')*->REC] [VAL] ]+
                """,
                table(subtable(row(
                        cell(VAL, rec(SR.and(tag("t1")).or(tag("t2")).unbounded())),
                        cell(VAL)).oneOrMore())));
    }

    @Test
    @DisplayName("Ad-hoc: distributed OR — A&(B|C) → (A&B)|(A&C)")
    void adhocDistributedOr() {
        assertMirrors(/* language=RTL */ """
                [ [VAL : (SR&(#'a'|#'b'))*->REC] [VAL] ]+
                """,
                table(subtable(row(
                        cell(VAL, rec(SR.and(tag("a").or(tag("b"))).unbounded())),
                        cell(VAL)).oneOrMore())));
    }

    @Test
    @DisplayName("Ad-hoc: settings prefix <NORM> maps to withTransformations(norm())")
    void adhocSettings() {
        assertMirrors(/* language=RTL */ """
                <NORM> [ [VAL : SR->REC] [VAL] ]+
                """,
                table(subtable(row(cell(VAL, rec(SR)), cell(VAL)).oneOrMore()))
                        .withTransformations(norm()));
    }

    @Test
    @DisplayName("Ad-hoc: cell-level actions before a bare conditional spec")
    void adhocCellLevelActs() {
        assertMirrors(/* language=RTL */ """
                [ [RT*->REC BLANK ? _ | VAL] [VAL] ]+
                """,
                table(subtable(row(
                        cell(acts(rec(RT.unbounded())), when(blank(), SKIP, VAL)),
                        cell(VAL)).oneOrMore())));
    }

    @Test
    @DisplayName("Ad-hoc: table-level condition and table-level inherited actions")
    void adhocTableLevel() {
        assertMirrors(/* language=RTL */ """
                !BLANK ? BW*->REC [ [VAL] ]+
                """,
                table(notBlank(), acts(rec(BW.unbounded())),
                        subtable(row(cell(VAL)).oneOrMore())));
    }

    @Test
    @DisplayName("Escape hatch: .where() on a provider builds a Custom filter term")
    void escapeHatch() {
        // No RTL equivalent by design — just verify it builds a valid pattern.
        TablePattern p = table(subtable(row(
                cell(VAL, rec(ROW.where("isNum", (a, c) -> c.str().matches("\\d+")).unbounded())),
                cell(VAL).oneOrMore())));
        assertEquals(1, p.subtablePatterns().size());
    }
}
