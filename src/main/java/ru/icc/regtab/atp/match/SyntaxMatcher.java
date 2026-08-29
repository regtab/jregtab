package ru.icc.regtab.atp.match;

import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ContentSpec;
import ru.icc.regtab.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.atp.spec.ItemDerivationDirective;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.Row;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Syntactic layer matching exactly following the formal algorithms from the paper.
 * <p>
 * Empty matches: a subrow (or subtable) pattern whose children all matched zero
 * elements matches the empty sequence, like {@code *} in regular expressions. Such an
 * iteration is recorded as an empty {@link MatchedSubrow}/{@link MatchedSubtable}, is
 * never repeated by {@code +}/{@code *}/{@code {n}}, and is tried at the end of the
 * sequence as well, so a trailing {@code { [BLANK]* }} matches a row without blanks.
 */
public final class SyntaxMatcher {

    private SyntaxMatcher() {
    }

    public static MatchResult match(TablePattern atp, TableSyntax syntax) {
        List<Row> rows = syntax.rows();
        if (!rowsSatisfyCondition(rows, 0, rows.size(), atp.condition())) {
            return MatchResult.failure();
        }
        MatchState state = new MatchState();
        MatchOutcome outcome = matchPatterns(atp.subtablePatterns(), rows, 0, state, StructureKind.ROW_SEQUENCE, -1);
        if (!outcome.success() || outcome.nextIndex() != rows.size()) {
            return MatchResult.failure();
        }
        return MatchResult.success(state.matchedPairs, state.matchedSubtables, state.matchedSubrows);
    }

    private static <P, E> MatchOutcome matchPatterns(
            List<P> patterns,
            List<E> elements,
            int elementIndex,
            MatchState state,
            StructureKind structureKind,
            int rowIndex) {

        int i = elementIndex;

        for (int j = 0; j < patterns.size(); j++) {
            P pattern = patterns.get(j);
            Quantifier quantifier = quantifierOf(pattern);
            int min = quantifier.min();
            int max = quantifier.max();
            Deque<StackEntry> stack = new ArrayDeque<>();

            // No "i < n" guard: a pattern that can match empty (a subrow/subtable whose
            // children are all optional) must be tried at the end of the sequence too;
            // cell and row dispatchers fail on their own when i >= n.
            while (stack.size() < max) {
                MatchSnapshot saved = state.snapshot();
                MatchOutcome dispatched = dispatchPattern(pattern, elements, i, state, structureKind, rowIndex);
                if (!dispatched.success()) {
                    state.restore(saved);
                    break;
                }
                stack.push(new StackEntry(i, saved));
                if (dispatched.nextIndex() == i) {
                    // Empty iteration: as in regex engines, an empty match is never repeated
                    // and satisfies any remaining lower bound ((a*)+ and (a*){3} match "").
                    min = 0;
                    break;
                }
                i = dispatched.nextIndex();
            }

            if (stack.size() < min) {
                return MatchOutcome.failure(i);
            }

            if (j < patterns.size() - 1 && !stack.isEmpty()) {
                while (true) {
                    MatchOutcome next = matchPatterns(
                            patterns.subList(j + 1, patterns.size()),
                            elements,
                            i,
                            state,
                            structureKind,
                            rowIndex);
                    if (next.success()) {
                        return next;
                    }
                    if (stack.size() <= min) {
                        return MatchOutcome.failure(i);
                    }
                    StackEntry released = stack.pop();
                    i = released.index();
                    state.restore(released.snapshot());
                }
            }
        }

        return MatchOutcome.success(i);
    }

    private static <P, E> MatchOutcome dispatchPattern(
            P pattern,
            List<E> elements,
            int elementIndex,
            MatchState state,
            StructureKind structureKind,
            int rowIndex) {

        if (pattern instanceof SubtablePattern subtablePattern) {
            return dispatchSubtablePattern(subtablePattern, castRows(elements), elementIndex, state);
        }
        if (pattern instanceof RowPattern rowPattern) {
            return dispatchRowPattern(rowPattern, castRows(elements), elementIndex, state);
        }
        if (pattern instanceof SubrowPattern subrowPattern) {
            return dispatchSubrowPattern(subrowPattern, castCells(elements), elementIndex, rowIndex, state);
        }
        if (pattern instanceof CellPattern cellPattern) {
            return dispatchCellPattern(cellPattern, castCells(elements), elementIndex, state);
        }
        throw new IllegalArgumentException("Unsupported ATP pattern type for " + structureKind + ": " + pattern.getClass().getName());
    }

    private static MatchOutcome dispatchSubtablePattern(
            SubtablePattern pattern,
            List<Row> rows,
            int rowIndex,
            MatchState state) {

        MatchSnapshot saved = state.snapshot();
        MatchOutcome inner = matchPatterns(pattern.rowPatterns(), rows, rowIndex, state, StructureKind.ROW_SEQUENCE, -1);
        if (!inner.success()) {
            state.restore(saved);
            return MatchOutcome.failure(rowIndex);
        }

        if (!rowsSatisfyCondition(rows, rowIndex, inner.nextIndex(), pattern.condition())) {
            state.restore(saved);
            return MatchOutcome.failure(rowIndex);
        }

        state.matchedSubtables.add(inner.nextIndex() == rowIndex
                ? MatchedSubtable.empty(pattern, rowIndex)
                : new MatchedSubtable(pattern, rowIndex, inner.nextIndex() - 1));
        return inner;
    }

    private static MatchOutcome dispatchRowPattern(
            RowPattern pattern,
            List<Row> rows,
            int rowIndex,
            MatchState state) {

        if (rowIndex >= rows.size()) {
            return MatchOutcome.failure(rowIndex);
        }

        Row row = rows.get(rowIndex);
        if (!rowSatisfiesCondition(row, pattern.condition())) {
            return MatchOutcome.failure(rowIndex);
        }

        List<Cell> cells = cellsOf(row);
        MatchSnapshot saved = state.snapshot();
        MatchOutcome inner = matchPatterns(pattern.subrowPatterns(), cells, 0, state, StructureKind.CELL_SEQUENCE, row.index());
        if (!inner.success() || inner.nextIndex() != cells.size()) {
            state.restore(saved);
            return MatchOutcome.failure(rowIndex);
        }

        return MatchOutcome.success(rowIndex + 1);
    }

    private static MatchOutcome dispatchSubrowPattern(
            SubrowPattern pattern,
            List<Cell> cells,
            int cellIndex,
            int rowIndex,
            MatchState state) {

        MatchSnapshot saved = state.snapshot();
        MatchOutcome inner = matchPatterns(pattern.cellPatterns(), cells, cellIndex, state, StructureKind.CELL_SEQUENCE, rowIndex);
        if (!inner.success()) {
            state.restore(saved);
            return MatchOutcome.failure(cellIndex);
        }

        if (!cellsSatisfyCondition(cells, cellIndex, inner.nextIndex(), pattern.condition())) {
            state.restore(saved);
            return MatchOutcome.failure(cellIndex);
        }

        int next = inner.nextIndex();
        if (next == cellIndex) {
            // Zero-width subrow: record where it matched (cells.size() at the end of the row).
            int col = cellIndex < cells.size() ? cells.get(cellIndex).col() : cells.size();
            state.matchedSubrows.add(MatchedSubrow.empty(pattern, rowIndex, col));
        } else {
            state.matchedSubrows.add(new MatchedSubrow(
                    pattern,
                    rowIndex,
                    cells.get(cellIndex).col(),
                    cells.get(next - 1).col()));
        }
        return inner;
    }

    private static MatchOutcome dispatchCellPattern(
            CellPattern pattern,
            List<Cell> cells,
            int cellIndex,
            MatchState state) {

        if (cellIndex >= cells.size()) {
            return MatchOutcome.failure(cellIndex);
        }

        Cell cell = cells.get(cellIndex);
        if (pattern.condition() != null && !pattern.condition().test(cell)) {
            return MatchOutcome.failure(cellIndex);
        }

        ContentSpec contentSpec = pattern.contentSpec();
        if (contentSpec != null && resolveIdd(contentSpec, cell) != ItemDerivationDirective.SKIP) {
            state.matchedPairs.add(new MatchedPair(pattern, cell));
        }

        return MatchOutcome.success(cellIndex + 1);
    }

    private static boolean rowsSatisfyCondition(
            List<Row> rows,
            int fromInclusive,
            int toExclusive,
            CellMatchCondition condition) {

        if (condition == null) {
            return true;
        }
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (!rowSatisfiesCondition(rows.get(i), condition)) {
                return false;
            }
        }
        return true;
    }

    private static boolean rowSatisfiesCondition(Row row, CellMatchCondition condition) {
        if (condition == null) {
            return true;
        }
        for (Cell cell : cellsOf(row)) {
            if (!condition.test(cell)) {
                return false;
            }
        }
        return true;
    }

    private static boolean cellsSatisfyCondition(
            List<Cell> cells,
            int fromInclusive,
            int toExclusive,
            CellMatchCondition condition) {

        if (condition == null) {
            return true;
        }
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (!condition.test(cells.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<Cell> cellsOf(Row row) {
        List<Cell> cells = new ArrayList<>();
        row.subrows().forEach(subrow -> cells.addAll(subrow.cells()));
        return cells;
    }

    private static ItemDerivationDirective resolveIdd(ContentSpec spec, Cell cell) {
        return switch (spec) {
            case AtomicContentSpec atomic -> atomic.idd();
            case DelimitedContentSpec delimited -> delimited.atomicSpec().idd();
            case CompoundContentSpec ignored -> ItemDerivationDirective.VAL;
            case ConditionalContentSpec conditional -> {
                ContentSpec branch = conditional.condition().test(cell)
                        ? conditional.positive()
                        : conditional.negative();
                yield resolveIdd(branch, cell);
            }
        };
    }

    private static Quantifier quantifierOf(Object pattern) {
        return switch (pattern) {
            case SubtablePattern p -> p.quantifier();
            case RowPattern p -> p.quantifier();
            case SubrowPattern p -> p.quantifier();
            case CellPattern p -> p.quantifier();
            default -> Quantifier.one();
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Row> castRows(List<?> elements) {
        return (List<Row>) elements;
    }

    @SuppressWarnings("unchecked")
    private static List<Cell> castCells(List<?> elements) {
        return (List<Cell>) elements;
    }

    private enum StructureKind {
        ROW_SEQUENCE,
        CELL_SEQUENCE
    }

    private record MatchOutcome(boolean success, int nextIndex) {
        private static MatchOutcome success(int nextIndex) {
            return new MatchOutcome(true, nextIndex);
        }

        private static MatchOutcome failure(int nextIndex) {
            return new MatchOutcome(false, nextIndex);
        }
    }

    private record StackEntry(int index, MatchSnapshot snapshot) {
    }

    private record MatchSnapshot(
            int matchedPairsSize,
            int matchedSubtablesSize,
            int matchedSubrowsSize) {
    }

    private static final class MatchState {
        private final List<MatchedPair> matchedPairs = new ArrayList<>();
        private final List<MatchedSubtable> matchedSubtables = new ArrayList<>();
        private final List<MatchedSubrow> matchedSubrows = new ArrayList<>();

        private MatchSnapshot snapshot() {
            return new MatchSnapshot(
                    matchedPairs.size(),
                    matchedSubtables.size(),
                    matchedSubrows.size());
        }

        private void restore(MatchSnapshot snapshot) {
            trimToSize(matchedPairs, snapshot.matchedPairsSize());
            trimToSize(matchedSubtables, snapshot.matchedSubtablesSize());
            trimToSize(matchedSubrows, snapshot.matchedSubrowsSize());
        }

        private static <T> void trimToSize(List<T> list, int size) {
            while (list.size() > size) {
                list.removeLast();
            }
        }
    }
}
