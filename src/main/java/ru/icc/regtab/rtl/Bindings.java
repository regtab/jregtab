package ru.icc.regtab.rtl;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.syntax.Cell;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Named Java predicates referenced from RTL via {@code EXT('name')}.
 *
 * <p>An {@code EXT('name')} constraint in a <em>cell match condition</em> position resolves
 * against a {@link #cell(String, Predicate) cell} binding; in a <em>provider constraint</em>
 * position it resolves against a {@link #filter(String, BiPredicate) filter} binding.
 * The two kinds form independent namespaces.
 *
 * <p>Instances are immutable; each {@code cell}/{@code filter} call returns a new object:
 * <pre>{@code
 * TablePattern p = RtlCompiler.compile("""
 *         { [ [VAL(EXT('isTotal')) : ST*->REC] []+ ] }+
 *         """,
 *         Bindings.of()
 *                 .cell("isTotal", c -> c.text().startsWith("Total"))
 *                 .filter("nearAnchor", (a, c) ->
 *                         Math.abs(a.cell().row() - c.cell().row()) <= 2));
 * }</pre>
 *
 * <p>Bindings that are never referenced by the compiled RTL string are permitted
 * (one {@code Bindings} object may serve several patterns).
 */
public final class Bindings {

    private static final Bindings EMPTY = new Bindings(Map.of(), Map.of());

    private final Map<String, Predicate<Cell>> cellBindings;
    private final Map<String, BiPredicate<CellDerivedItem, CellDerivedItem>> filterBindings;

    private Bindings(Map<String, Predicate<Cell>> cellBindings,
                     Map<String, BiPredicate<CellDerivedItem, CellDerivedItem>> filterBindings) {
        this.cellBindings = cellBindings;
        this.filterBindings = filterBindings;
    }

    /** Empty bindings — the starting point for chaining. */
    public static Bindings of() {
        return EMPTY;
    }

    /**
     * Returns a copy with a cell predicate bound under {@code name} — the target of
     * {@code EXT('name')} in a cell match condition position.
     *
     * @throws IllegalArgumentException if {@code name} is blank or already bound as a cell predicate
     */
    public Bindings cell(String name, Predicate<Cell> predicate) {
        requireName(name);
        Objects.requireNonNull(predicate, "predicate");
        if (cellBindings.containsKey(name))
            throw new IllegalArgumentException("Duplicate cell binding: '" + name + "'");
        Map<String, Predicate<Cell>> cells = new java.util.LinkedHashMap<>(cellBindings);
        cells.put(name, predicate);
        return new Bindings(Map.copyOf(cells), filterBindings);
    }

    /**
     * Returns a copy with an item filter bound under {@code name} — the target of
     * {@code EXT('name')} in a provider constraint position. The first argument of the
     * predicate is the anchor item, the second is the candidate item.
     *
     * @throws IllegalArgumentException if {@code name} is blank or already bound as a filter
     */
    public Bindings filter(String name, BiPredicate<CellDerivedItem, CellDerivedItem> predicate) {
        requireName(name);
        Objects.requireNonNull(predicate, "predicate");
        if (filterBindings.containsKey(name))
            throw new IllegalArgumentException("Duplicate filter binding: '" + name + "'");
        Map<String, BiPredicate<CellDerivedItem, CellDerivedItem>> filters =
                new java.util.LinkedHashMap<>(filterBindings);
        filters.put(name, predicate);
        return new Bindings(cellBindings, Map.copyOf(filters));
    }

    /** Cell predicate bound under {@code name}, or {@code null}. Used by the compiler. */
    public Predicate<Cell> cellPredicate(String name) {
        return cellBindings.get(name);
    }

    /** Item filter bound under {@code name}, or {@code null}. Used by the compiler. */
    public BiPredicate<CellDerivedItem, CellDerivedItem> itemFilter(String name) {
        return filterBindings.get(name);
    }

    private static void requireName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank())
            throw new IllegalArgumentException("Binding name must not be blank");
    }
}
