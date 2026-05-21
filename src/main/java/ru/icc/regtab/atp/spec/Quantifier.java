package ru.icc.regtab.atp.spec;

import java.util.Objects;

/**
 * Quantifier (def:atp:quantifier): specifies how many consecutive substructures
 * at a given level are matched.
 * <p>
 * {@code ?} — zero or one; {@code 1} — exactly one (default);
 * {@code {n}} — exactly n; {@code +} — one or more; {@code *} — zero or more.
 */
public record Quantifier(Kind kind, int n) {

    public enum Kind {
        /** {@code ?} — zero or one occurrence. */
        ZERO_OR_ONE,
        /** {@code 1} — exactly one occurrence (default). */
        ONE,
        /** {@code {n}} — exactly n occurrences (n ≥ 2). */
        EXACTLY,
        /** {@code +} — one or more occurrences. */
        ONE_OR_MORE,
        /** {@code *} — zero or more occurrences. */
        ZERO_OR_MORE
    }

    /** Represents unbounded maximum. */
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    // Singleton constants for non-parameterized quantifiers
    private static final Quantifier ZERO_OR_ONE = new Quantifier(Kind.ZERO_OR_ONE, 0);
    private static final Quantifier ONE = new Quantifier(Kind.ONE, 0);
    private static final Quantifier ONE_OR_MORE = new Quantifier(Kind.ONE_OR_MORE, 0);
    private static final Quantifier ZERO_OR_MORE = new Quantifier(Kind.ZERO_OR_MORE, 0);

    public Quantifier {
        Objects.requireNonNull(kind, "kind");
        if (kind == Kind.EXACTLY && n < 2) {
            throw new IllegalArgumentException("EXACTLY requires n >= 2, got: " + n);
        }
    }

    public static Quantifier zeroOrOne() { return ZERO_OR_ONE; }
    public static Quantifier one() { return ONE; }
    public static Quantifier oneOrMore() { return ONE_OR_MORE; }
    public static Quantifier zeroOrMore() { return ZERO_OR_MORE; }
    public static Quantifier exactly(int n) { return new Quantifier(Kind.EXACTLY, n); }

    /** Minimum number of occurrences required. */
    public int min() {
        return switch (kind) {
            case ZERO_OR_ONE, ZERO_OR_MORE -> 0;
            case ONE, ONE_OR_MORE -> 1;
            case EXACTLY -> n;
        };
    }

    /** Maximum number of occurrences allowed ({@link #UNBOUNDED} for ∞). */
    public int max() {
        return switch (kind) {
            case ZERO_OR_ONE, ONE -> 1;
            case EXACTLY -> n;
            case ONE_OR_MORE, ZERO_OR_MORE -> UNBOUNDED;
        };
    }
}
