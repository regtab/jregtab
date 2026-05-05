package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.atp.match.MatchResult;
import ru.icc.regtab.itm.atp.match.MatchedSubrow;
import ru.icc.regtab.itm.atp.match.SemanticConstructor;
import ru.icc.regtab.itm.atp.match.SyntaxMatcher;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.model.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import java.util.Objects;
import java.util.Optional;
import java.util.Comparator;
import java.util.Set;

/**
 * Facade for matching an Abstract Table Pattern (ATP) against an ITM instance.
 * <p>
 * Given a {@link TablePattern} and a {@link TableSyntax} whose syntactic layer
 * is populated, this class determines whether the table belongs to the class
 * described by the pattern. If matching succeeds, the semantic layer is
 * populated automatically and an {@link InterpretableTable} is returned.
 * <p>
 * Usage:
 * <pre>{@code
 * TablePattern atp = TablePattern.of(
 *     SubtablePattern.of(Quantifier.oneOrMore(),
 *         RowPattern.of(
 *             CellPattern.of(AtomicContentSpec.val(ActionSpec.rec(...))),
 *             CellPattern.of(AtomicContentSpec.val())
 *         )
 *     )
 * );
 *
 * Optional<InterpretableTable> itm = AtpMatcher.match(atp, syntax);
 * itm.ifPresent(t -> {
 *     Recordset rs = TableInterpreter.interpret(t);
 *     // ...
 * });
 * }</pre>
 */
public final class AtpMatcher {

    private AtpMatcher() {}

    /**
     * Matches an ATP instance against an ITM syntactic layer.
     * On success, populates the semantic layer and returns an InterpretableTable.
     * On failure, returns empty.
     *
     * @param atp    the Abstract Table Pattern
     * @param syntax the ITM syntactic layer (populated)
     * @return InterpretableTable if match succeeds, empty otherwise
     */
    public static Optional<InterpretableTable> match(TablePattern atp, TableSyntax syntax) {
        return match(atp, syntax, Set.of());
    }

    /**
     * Matches an ATP instance against an ITM syntactic layer with context items.
     *
     * @param atp          the Abstract Table Pattern
     * @param syntax       the ITM syntactic layer (populated)
     * @param contextItems context-derived items to include in the semantic layer
     * @return InterpretableTable if match succeeds, empty otherwise
     */
    public static Optional<InterpretableTable> match(
            TablePattern atp, TableSyntax syntax, Set<ContextDerivedItem> contextItems) {
        Objects.requireNonNull(atp, "atp");
        Objects.requireNonNull(syntax, "syntax");
        Objects.requireNonNull(contextItems, "contextItems");

        MatchResult result = SyntaxMatcher.match(atp, syntax);
        if (!result.success()) {
            return Optional.empty();
        }

        try {
            applyMatchedStructure(syntax, result);
            InterpretableTable itm = SemanticConstructor.construct(
                    syntax, result.matchedPairs(), contextItems);
            return Optional.of(itm);
        } catch (SemanticConstructor.MatchException e) {
            return Optional.empty();
        }
    }

    private static void applyMatchedStructure(TableSyntax syntax, MatchResult result) {
        int[] starts = result.matchedSubtables().stream()
                .mapToInt(m -> m.rowStart())
                .distinct()
                .sorted()
                .toArray();
        if (starts.length > 0) {
            syntax.defineSubtables(starts);
        }

        result.matchedSubrows().stream()
                .sorted(Comparator.comparingInt(MatchedSubrow::rowIndex)
                        .thenComparingInt(MatchedSubrow::colStart))
                .forEach(m -> syntax.defineSubrow(m.rowIndex(), m.colStart(), m.colEnd()));
    }
}
