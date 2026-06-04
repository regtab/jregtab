package ru.icc.regtab.atp;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.match.MatchResult;
import ru.icc.regtab.atp.match.SyntaxMatcher;
import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SyntaxMatcher (Algorithms 1 & 2).
 */
class SyntaxMatcherTest {

    /** Helper: creates a simple table with text content. */
    private static TableSyntax table(String[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        var syntax = new TableSyntax(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                syntax.getCell(r, c).setText(data[r][c]);
            }
        }
        return syntax;
    }

    @Test
    void simpleExactMatch_oneSubtableOneRowTwoCells() {
        var syntax = table(new String[][]{{"A", "B"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(2, result.matchedPairs().size());
        assertEquals("A", result.matchedPairs().get(0).cell().text());
        assertEquals("B", result.matchedPairs().get(1).cell().text());
    }

    @Test
    void matchFails_tooFewCells() {
        var syntax = table(new String[][]{{"A"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertFalse(result.success());
    }

    @Test
    void matchFails_tooManyCells() {
        var syntax = table(new String[][]{{"A", "B", "C"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertFalse(result.success());
    }

    @Test
    void skipCellNotInMatchedPairs() {
        var syntax = table(new String[][]{{"A", "B", "C"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(2, result.matchedPairs().size());
        assertEquals("A", result.matchedPairs().get(0).cell().text());
        assertEquals("C", result.matchedPairs().get(1).cell().text());
    }

    @Test
    void oneOrMoreCells_greedy() {
        var syntax = table(new String[][]{{"A", "B", "C", "D"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(4, result.matchedPairs().size());
    }

    @Test
    void zeroOrMoreCells_consumesAll() {
        var syntax = table(new String[][]{{"A", "B"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.zeroOrMore(), AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(2, result.matchedPairs().size());
    }

    @Test
    void zeroOrMoreCells_matchesEmpty() {
        var syntax = table(new String[][]{{"A"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.zeroOrMore(), AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(1, result.matchedPairs().size());
    }

    @Test
    void backtracking_oneOrMoreThenFixed() {
        // 4 cells: oneOrMore should consume 3, leaving 1 for the fixed pattern
        var syntax = table(new String[][]{{"A", "B", "C", "D"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(4, result.matchedPairs().size());
        // Last matched pair should be "D" (the fixed cell)
        assertEquals("D", result.matchedPairs().getLast().cell().text());
    }

    @Test
    void backtracking_zeroOrOneThenFixed() {
        // 1 cell: zeroOrOne consumes it greedily, then backtracks to give it to the fixed pattern
        var syntax = table(new String[][]{{"A"}});
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(Quantifier.zeroOrOne(), AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(1, result.matchedPairs().size());
        assertEquals("A", result.matchedPairs().getFirst().cell().text());
    }

    @Test
    void cellMatchCondition_filtersRows() {
        var syntax = table(new String[][]{{"A", "1"}, {"B", "2"}, {"C", "3"}});

        // Pattern: oneOrMore subtables with one row where first cell is not blank
        var atp = TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(6, result.matchedPairs().size());
    }

    @Test
    void cellMatchCondition_failsOnMismatch() {
        var syntax = table(new String[][]{{"A", "B"}});
        var cond = new CellMatchCondition(new CellPredicate.Custom("equalsX", c -> c.text().equals("X")));

        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(cond, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertFalse(result.success());
    }

    @Test
    void multipleRowTypes_twoRowsPerSubtable() {
        var syntax = table(new String[][]{
                {"H1", "H2"},
                {"D1", "D2"},
                {"H3", "H4"},
                {"D3", "D4"}
        });

        var atp = TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(CellPattern.of(AtomicContentSpec.val()), CellPattern.of(AtomicContentSpec.val())),
                        RowPattern.of(CellPattern.skip(), CellPattern.of(AtomicContentSpec.val()))
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        // 2 subtables × (2 val + 1 val) = 6 matched pairs
        assertEquals(6, result.matchedPairs().size());
    }

    @Test
    void exactlyQuantifier_rows() {
        var syntax = table(new String[][]{
                {"A"}, {"B"}, {"C"}
        });

        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.exactly(3),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(3, result.matchedPairs().size());
    }

    @Test
    void exactlyQuantifier_tooFew() {
        var syntax = table(new String[][]{
                {"A"}, {"B"}
        });

        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.exactly(3),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertFalse(result.success());
    }

    // -------- T1–T7: table-level λ and conjunction semantics --------

    @Test
    void T1_tableLambda_absent_noEffect() {
        var syntax = table(new String[][]{{"A", "B"}, {"C", "D"}});
        var atp = TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(CellPattern.of(AtomicContentSpec.val()),
                                      CellPattern.of(AtomicContentSpec.val()))
                )
        );
        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        assertEquals(4, result.matchedPairs().size());
    }

    @Test
    void T2_tableLambda_cellLevelOnly_rejectsNonMatching() {
        var syntax = table(new String[][]{{"abc", "2"}});
        var numeric = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(numeric, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T3_tableLambda_onTable_allNonBlank_succeeds() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var syntax = table(new String[][]{{"A", "B"}});
        var atp = new TablePattern(notBlank,
                List.of(SubtablePattern.of(
                        RowPattern.of(CellPattern.of(AtomicContentSpec.val()),
                                      CellPattern.of(AtomicContentSpec.val())))),
                List.of());
        assertTrue(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T3b_tableLambda_onTable_blankCellPresent_fails() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var syntax = table(new String[][]{{"A", ""}});
        var atp = new TablePattern(notBlank,
                List.of(SubtablePattern.of(
                        RowPattern.of(CellPattern.of(AtomicContentSpec.val()),
                                      CellPattern.of(AtomicContentSpec.val())))),
                List.of());
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T4_tableLambda_tableAndCell_conjunctionBothPass() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var numeric  = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
        var syntax = table(new String[][]{{"42"}});
        var atp = new TablePattern(notBlank,
                List.of(SubtablePattern.of(
                        RowPattern.of(CellPattern.of(numeric, Quantifier.one(), AtomicContentSpec.val())))),
                List.of());
        assertTrue(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T4b_tableLambda_tableAndCell_tableLambdaFails() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var numeric  = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
        var syntax = table(new String[][]{{"", "2"}});
        var atp = new TablePattern(notBlank,
                List.of(SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(numeric, Quantifier.one(), AtomicContentSpec.val()),
                                CellPattern.of(AtomicContentSpec.val())))),
                List.of());
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T5_tableLambda_threeAncestorLevels_allPass() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var numeric  = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
        var syntax = table(new String[][]{{"1", "2"}});
        var rowPat     = RowPattern.of(numeric, Quantifier.one(),
                CellPattern.of(AtomicContentSpec.val()), CellPattern.of(AtomicContentSpec.val()));
        var subtablePat = new SubtablePattern(numeric, Quantifier.one(), List.of(rowPat));
        var atp = new TablePattern(notBlank, List.of(subtablePat), List.of());
        assertTrue(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T5b_tableLambda_threeAncestorLevels_midLevelFails() {
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var numeric  = new CellMatchCondition(new CellPredicate.RegexMatched("\\d+"));
        var syntax = table(new String[][]{{"1", "abc"}});
        var rowPat     = RowPattern.of(numeric, Quantifier.one(),
                CellPattern.of(AtomicContentSpec.val()), CellPattern.of(AtomicContentSpec.val()));
        var subtablePat = new SubtablePattern(numeric, Quantifier.one(), List.of(rowPat));
        var atp = new TablePattern(notBlank, List.of(subtablePat), List.of());
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T6_tableLambda_contradictingConditions_fails() {
        // λ_table = NOT_BLANK, λ_cell = BLANK: non-blank cell fails cell λ; blank cell fails table pre-check
        var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
        var blank    = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
        var syntax = table(new String[][]{{"A"}});
        var atp = new TablePattern(notBlank,
                List.of(SubtablePattern.of(
                        RowPattern.of(CellPattern.of(blank, Quantifier.one(), AtomicContentSpec.val())))),
                List.of());
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void T7_tableLambda_preCheck_rejectsBeforeStructuralMatch() {
        // Table λ = BLANK; cells are "A","B" (non-blank). The structural pattern [VAL][VAL]
        // would succeed without a pre-check. Pre-check must reject immediately.
        var blank = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
        var syntax = table(new String[][]{{"A", "B"}});
        var atp = new TablePattern(blank,
                List.of(SubtablePattern.of(
                        RowPattern.of(CellPattern.of(AtomicContentSpec.val()),
                                      CellPattern.of(AtomicContentSpec.val())))),
                List.of());
        assertFalse(SyntaxMatcher.match(atp, syntax).success());
    }

    @Test
    void conditionalContentSpec_branchSelection() {
        var syntax = table(new String[][]{{"ABC", ""}});

        var cond = new ConditionalContentSpec(
                new CellMatchCondition(new CellPredicate.Custom("notEmpty", c -> !c.text().isEmpty())),
                AtomicContentSpec.val(),
                AtomicContentSpec.skip()
        );

        var atp = TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(cond),
                                CellPattern.of(cond)
                        )
                )
        );

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        assertTrue(result.success());
        // Only "ABC" matches positive branch (val), "" matches negative (skip)
        assertEquals(1, result.matchedPairs().size());
        assertEquals("ABC", result.matchedPairs().getFirst().cell().text());
    }
}
