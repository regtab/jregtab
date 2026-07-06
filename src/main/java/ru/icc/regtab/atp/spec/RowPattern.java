package ru.icc.regtab.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Row pattern (def:rp):
 * P_row = (λ, q, ⟨P_sr¹, …, P_srᵏ⟩), k ≥ 1.
 *
 * @param condition  optional cell match condition λ (null if absent)
 * @param quantifier quantifier q (default: ONE)
 * @param subrowPatterns ordered sequence of subrow patterns P_sr (≥ 1)
 */
public record RowPattern(
        CellMatchCondition condition,
        Quantifier quantifier,
        List<SubrowPattern> subrowPatterns
) {
    public RowPattern {
        if (quantifier == null) quantifier = Quantifier.one();
        subrowPatterns = List.copyOf(Objects.requireNonNull(subrowPatterns, "subrowPatterns"));
        if (subrowPatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one subrow pattern is required");
        }
    }

    /** Convenience: row with one subrow containing the given cell patterns. */
    public static RowPattern of(CellPattern... cells) {
        return new RowPattern(null, Quantifier.one(),
                List.of(SubrowPattern.of(cells)));
    }

    /** Convenience: row with quantifier and one subrow. */
    public static RowPattern of(Quantifier q, CellPattern... cells) {
        return new RowPattern(null, q,
                List.of(SubrowPattern.of(cells)));
    }

    /** Convenience: row with explicit subrow patterns. */
    public static RowPattern of(Quantifier q, SubrowPattern... subrows) {
        return new RowPattern(null, q, List.of(subrows));
    }

    /** Convenience: row with condition, quantifier, and one subrow. */
    public static RowPattern of(CellMatchCondition cond, Quantifier q, CellPattern... cells) {
        return new RowPattern(cond, q,
                List.of(SubrowPattern.of(cells)));
    }

    /** Copy with quantifier {@code +} (RTL postfix). */
    public RowPattern oneOrMore() { return new RowPattern(condition, Quantifier.oneOrMore(), subrowPatterns); }

    /** Copy with quantifier {@code *} (RTL postfix). */
    public RowPattern zeroOrMore() { return new RowPattern(condition, Quantifier.zeroOrMore(), subrowPatterns); }

    /** Copy with quantifier {@code ?} (RTL postfix). */
    public RowPattern zeroOrOne() { return new RowPattern(condition, Quantifier.zeroOrOne(), subrowPatterns); }

    /** Copy with quantifier {@code {n}} (RTL postfix). */
    public RowPattern exactly(int n) { return new RowPattern(condition, Quantifier.exactly(n), subrowPatterns); }
}
