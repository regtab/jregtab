package ru.icc.regtab.itm.atp;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.atp.match.MatchResult;
import ru.icc.regtab.itm.atp.match.SyntaxMatcher;
import ru.icc.regtab.itm.atp.spec.*;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

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
        var cond = new CellMatchCondition(c -> c.text().equals("X"));

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

    @Test
    void conditionalContentSpec_branchSelection() {
        var syntax = table(new String[][]{{"ABC", ""}});

        var cond = new ConditionalContentSpec(
                new CellMatchCondition(c -> !c.text().isEmpty()),
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
