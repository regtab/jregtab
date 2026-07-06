package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.syntax.Cell;

import java.util.function.Predicate;

/**
 * Specification of a cell match predicate — structured, serializable alternative to
 * opaque {@link Predicate}{@code <Cell>} lambdas.
 *
 * <p>Mirrors the RTL grammar's {@code cellMatchConstr}: {@code regex | blank}.
 * {@code CONTAINS} is an extension added in this branch.
 */
public sealed interface CellPredicate permits
        CellPredicate.Blank,
        CellPredicate.NotBlank,
        CellPredicate.RegexMatched,
        CellPredicate.NotRegexMatched,
        CellPredicate.Contains,
        CellPredicate.NotContains,
        CellPredicate.External,
        CellPredicate.Custom {

    /** RTL representation of this predicate. */
    String toRtl();

    /** Produces the runtime behavioral predicate from this specification. */
    Predicate<Cell> toPredicate();

    // ---- Variants ----

    record Blank() implements CellPredicate {
        public static final Blank INSTANCE = new Blank();
        public String toRtl() { return "BLANK"; }
        public Predicate<Cell> toPredicate() { return Cell::textBlank; }
    }

    record NotBlank() implements CellPredicate {
        public static final NotBlank INSTANCE = new NotBlank();
        public String toRtl() { return "!BLANK"; }
        public Predicate<Cell> toPredicate() { return c -> !c.textBlank(); }
    }

    record RegexMatched(String pattern) implements CellPredicate {
        public String toRtl() { return "\"" + pattern + "\""; }
        public Predicate<Cell> toPredicate() { return c -> c.text().matches(pattern); }
    }

    record NotRegexMatched(String pattern) implements CellPredicate {
        public String toRtl() { return "!\"" + pattern + "\""; }
        public Predicate<Cell> toPredicate() { return c -> !c.text().matches(pattern); }
    }

    record Contains(String substring) implements CellPredicate {
        public String toRtl() { return "~\"" + substring + "\""; }
        public Predicate<Cell> toPredicate() { return c -> c.text().contains(substring); }
    }

    record NotContains(String substring) implements CellPredicate {
        public String toRtl() { return "!~\"" + substring + "\""; }
        public Predicate<Cell> toPredicate() { return c -> !c.text().contains(substring); }
    }

    /**
     * Externally bound predicate — RTL analog {@code EXT('name')}, resolved from
     * {@code Bindings} passed to the RTL compiler. Unlike {@link Custom}, it is
     * serializable back to RTL because the name identifies the binding.
     */
    record External(String name, Predicate<Cell> predicate) implements CellPredicate {
        public String toRtl() { return "EXT('" + name + "')"; }
        public Predicate<Cell> toPredicate() { return predicate; }
    }

    /** Escape hatch — {@code toRtl()} throws {@link UnsupportedOperationException}. */
    record Custom(String description, Predicate<Cell> predicate) implements CellPredicate {
        public String toRtl() {
            throw new UnsupportedOperationException("Custom CellPredicate has no RTL analog: " + description);
        }
        public Predicate<Cell> toPredicate() { return predicate; }
    }
}
