package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;

import java.util.function.BiPredicate;

/**
 * Atomic constraint — building block of {@link ItemFilterConditionSpec}.
 * Mirrors the RTL grammar's {@code spatConstr} and {@code contConstr} rules.
 */
public sealed interface FilterTerm permits
        FilterTerm.LeftOf,
        FilterTerm.RightOf,
        FilterTerm.Above,
        FilterTerm.Below,
        FilterTerm.SameSubrow,
        FilterTerm.SameSubcol,
        FilterTerm.SameSubtable,
        FilterTerm.SameRow,
        FilterTerm.SameCol,
        FilterTerm.NotSameCell,
        FilterTerm.SameCell,
        FilterTerm.ColExact,
        FilterTerm.ColOffset,
        FilterTerm.ColRange,
        FilterTerm.ColAbsoluteRange,
        FilterTerm.RowExact,
        FilterTerm.RowOffset,
        FilterTerm.RowAbsoluteRange,
        FilterTerm.PosExact,
        FilterTerm.PosOffset,
        FilterTerm.PosRange,
        FilterTerm.RegexMatched,
        FilterTerm.NotRegexMatched,
        FilterTerm.Contains,
        FilterTerm.NotContains,
        FilterTerm.Blank,
        FilterTerm.NotBlank,
        FilterTerm.Tagged,
        FilterTerm.NotTagged,
        FilterTerm.SameStr,
        FilterTerm.External,
        FilterTerm.Custom {

    /** RTL token for this constraint (e.g. {@code "ST"}, {@code "BW"}). */
    String toRtl();

    /** Behavioral equivalent for runtime filtering. */
    ItemFilterCondition toCondition();

    // ---- Named spatial singletons ----

    record LeftOf() implements FilterTerm {
        public static final LeftOf INSTANCE = new LeftOf();
        public String toRtl() { return "LT"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubrow(a) && c.cell().col() < a.cell().col();
        }
    }

    record RightOf() implements FilterTerm {
        public static final RightOf INSTANCE = new RightOf();
        public String toRtl() { return "RT"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubrow(a) && c.cell().col() > a.cell().col();
        }
    }

    record Above() implements FilterTerm {
        public static final Above INSTANCE = new Above();
        public String toRtl() { return "AV"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubcol(a) && c.cell().row() < a.cell().row();
        }
    }

    record Below() implements FilterTerm {
        public static final Below INSTANCE = new Below();
        public String toRtl() { return "BW"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubcol(a) && c.cell().row() > a.cell().row();
        }
    }

    record SameSubrow() implements FilterTerm {
        public static final SameSubrow INSTANCE = new SameSubrow();
        public String toRtl() { return "SR"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubrow(a) && !c.sameCell(a);
        }
    }

    record SameSubcol() implements FilterTerm {
        public static final SameSubcol INSTANCE = new SameSubcol();
        public String toRtl() { return "SC"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubcol(a) && !c.sameCell(a);
        }
    }

    record SameSubtable() implements FilterTerm {
        public static final SameSubtable INSTANCE = new SameSubtable();
        public String toRtl() { return "ST"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameSubtable(a) && !c.sameCell(a);
        }
    }

    record SameRow() implements FilterTerm {
        public static final SameRow INSTANCE = new SameRow();
        public String toRtl() { return "ROW"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameRow(a) && !c.sameCell(a);
        }
    }

    record SameCol() implements FilterTerm {
        public static final SameCol INSTANCE = new SameCol();
        public String toRtl() { return "COL"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameCol(a) && !c.sameCell(a);
        }
    }

    record NotSameCell() implements FilterTerm {
        public static final NotSameCell INSTANCE = new NotSameCell();
        public String toRtl() { return "NCL"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> !c.sameCell(a);
        }
    }

    record SameCell() implements FilterTerm {
        public static final SameCell INSTANCE = new SameCell();
        public String toRtl() { return "CL"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameCell(a);
        }
    }

    // ---- Positional spatial ----

    record ColExact(int n) implements FilterTerm {
        public String toRtl() { return "C" + n; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().col() == n;
        }
    }

    record ColOffset(int delta) implements FilterTerm {
        public String toRtl() {
            return delta >= 0 ? "C+" + delta : "C" + delta;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().col() == a.cell().col() + delta;
        }
    }

    /** Range C{from}..{to}. Use {@code to = Integer.MAX_VALUE} for an open end (C+n..). */
    record ColRange(int from, int to) implements FilterTerm {
        public String toRtl() {
            String loStr = from >= 0 ? "C+" + from : "C" + from;
            return to == Integer.MAX_VALUE ? loStr + ".." : loStr + ".." + to;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> {
                int col = c.cell().col();
                int lo  = a.cell().col() + from;
                return col >= lo && (to == Integer.MAX_VALUE || col <= a.cell().col() + to);
            };
        }
    }

    /** Absolute column range Ca..b — column is between lo and hi (inclusive, 0-based). */
    record ColAbsoluteRange(int lo, int hi) implements FilterTerm {
        public String toRtl() {
            return hi == Integer.MAX_VALUE ? "C" + lo + ".." : "C" + lo + ".." + hi;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().col() >= lo && (hi == Integer.MAX_VALUE || c.cell().col() <= hi);
        }
    }

    record RowExact(int n) implements FilterTerm {
        public String toRtl() { return "R" + n; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().row() == n;
        }
    }

    record RowOffset(int delta) implements FilterTerm {
        public String toRtl() {
            return delta >= 0 ? "R+" + delta : "R" + delta;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().row() == a.cell().row() + delta;
        }
    }

    /** Absolute row range Ra..b — row is between lo and hi (inclusive, 0-based). */
    record RowAbsoluteRange(int lo, int hi) implements FilterTerm {
        public String toRtl() {
            return hi == Integer.MAX_VALUE ? "R" + lo + ".." : "R" + lo + ".." + hi;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.cell().row() >= lo && (hi == Integer.MAX_VALUE || c.cell().row() <= hi);
        }
    }

    record PosExact(int n) implements FilterTerm {
        public String toRtl() { return "P" + n; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.index() == n;
        }
    }

    record PosOffset(int delta) implements FilterTerm {
        public String toRtl() {
            return delta >= 0 ? "P+" + delta : "P" + delta;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.index() == a.index() + delta;
        }
    }

    /** Absolute position range Pa..b — index is between lo and hi (inclusive). */
    record PosRange(int lo, int hi) implements FilterTerm {
        public String toRtl() {
            return hi == Integer.MAX_VALUE ? "P" + lo + ".." : "P" + lo + ".." + hi;
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.index() >= lo && (hi == Integer.MAX_VALUE || c.index() <= hi);
        }
    }

    // ---- Content ----

    record RegexMatched(String pattern) implements FilterTerm {
        public String toRtl() { return "\"" + pattern + "\""; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.str().matches(pattern);
        }
    }

    record NotRegexMatched(String pattern) implements FilterTerm {
        public String toRtl() { return "!\"" + pattern + "\""; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> !c.str().matches(pattern);
        }
    }

    record Contains(String substring) implements FilterTerm {
        public String toRtl() { return "~\"" + substring + "\""; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.str().contains(substring);
        }
    }

    record NotContains(String substring) implements FilterTerm {
        public String toRtl() { return "!~\"" + substring + "\""; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> !c.str().contains(substring);
        }
    }

    record Blank() implements FilterTerm {
        public static final Blank INSTANCE = new Blank();
        public String toRtl() { return "BLANK"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.blankStr();
        }
    }

    record NotBlank() implements FilterTerm {
        public static final NotBlank INSTANCE = new NotBlank();
        public String toRtl() { return "!BLANK"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> !c.blankStr();
        }
    }

    record Tagged(String tag) implements FilterTerm {
        public String toRtl() { return quoted(tag); }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.hasTag(tag);
        }
    }

    record NotTagged(String tag) implements FilterTerm {
        public String toRtl() { return "!" + quoted(tag); }
        public ItemFilterCondition toCondition() {
            return (a, c) -> !c.hasTag(tag);
        }
    }

    /** Tags are stored with the leading {@code #}; the RTL form is always quoted: {@code #'name'}. */
    private static String quoted(String tag) {
        String name = tag.startsWith("#") ? tag.substring(1) : tag;
        return "#'" + name + "'";
    }

    record SameStr() implements FilterTerm {
        public static final SameStr INSTANCE = new SameStr();
        public String toRtl() { return "STR"; }
        public ItemFilterCondition toCondition() {
            return (a, c) -> c.sameStr(a);
        }
    }

    /**
     * Externally bound item filter — RTL analog {@code EXT('name')}, resolved from
     * {@code Bindings} passed to the RTL compiler. Unlike {@link Custom}, it is
     * serializable back to RTL because the name identifies the binding.
     */
    record External(String name,
                    BiPredicate<CellDerivedItem, CellDerivedItem> predicate) implements FilterTerm {
        public String toRtl() { return "EXT('" + name + "')"; }
        public ItemFilterCondition toCondition() { return predicate::test; }
    }

    /** Escape hatch — {@code toRtl()} throws {@link UnsupportedOperationException}. */
    record Custom(String description,
                  BiPredicate<CellDerivedItem, CellDerivedItem> predicate) implements FilterTerm {
        public String toRtl() { throw new UnsupportedOperationException("Custom constraint has no RTL analog"); }
        public ItemFilterCondition toCondition() { return predicate::test; }
    }
}
