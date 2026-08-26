package ru.icc.regtab.itm.semantics.provider;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.spec.CandidateScopes;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the optimised {@link CellDerivedItemProvider} (index + candidate scope + early exit) to the
 * reference definition Υ^{J,k}_{τ,κ}(anchor) = Ω_τ(Φ_κ(anchor, J \ {anchor}))[:k] — a full scan of J
 * through {@link ItemFilter#apply} and {@link ItemLinearization#sort} — on random tables, for every
 * filter term the scope derivation knows about, every traversal order, cardinality and provider kind.
 */
class CellDerivedItemProviderEquivalenceTest {

    private static final int[] CARDINALITIES = {0, 1, 2, 3, CellDerivedItemProvider.UNBOUNDED};

    @Test
    void optimisedProviderMatchesReferenceDefinition() {
        Random rnd = new Random(20260826L);
        for (int table = 0; table < 25; table++) {
            int numRows = 1 + rnd.nextInt(9);
            int numCols = 1 + rnd.nextInt(7);
            TableSyntax syntax = randomStructure(rnd, numRows, numCols);
            Set<CellDerivedItem> items = randomItems(rnd, syntax, numRows, numCols);
            if (items.isEmpty()) continue;
            CellDerivedItemIndex index = new CellDerivedItemIndex(items);
            List<CellDerivedItem> anchors = new ArrayList<>(items);

            for (ItemFilterConditionSpec spec : specs(rnd)) {
                ItemFilterCondition kappa = spec.toCondition();
                CandidateScope scope = CandidateScopes.of(spec);
                for (TraversalOrder tau : TraversalOrder.values()) {
                    for (int k : CARDINALITIES) {
                        for (CellDerivedProviderKind kind : CellDerivedProviderKind.values()) {
                            for (boolean excludeAnchor : new boolean[]{true, false}) {
                                var provider = new CellDerivedItemProvider(
                                        new ItemFilter(kappa), new ItemLinearization(tau),
                                        index, scope, k, kind, excludeAnchor, true);
                                for (CellDerivedItem anchor : anchors) {
                                    List<CellDerivedItem> expected =
                                            reference(items, anchor, kappa, tau, k, kind, excludeAnchor);
                                    if (expected == null) continue;   // incompatible anchor (lenient → empty)
                                    assertEquals(expected, provider.provide(anchor),
                                            () -> "spec=" + describe(spec) + " τ=" + tau + " k=" + k
                                                    + " kind=" + kind + " anchor=" + anchor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void indexIsRebuiltWhenTargetSetGrows() {
        TableSyntax syntax = new TableSyntax(3, 3);
        Set<CellDerivedItem> items = new LinkedHashSet<>();
        CellDerivedItem a = item(syntax, 1, 1, 0, ItemType.VALUE);
        items.add(a);
        var provider = new CellDerivedItemProvider(
                new ItemFilter(FilterTerm.SameRow.INSTANCE.toCondition()),
                new ItemLinearization(TraversalOrder.ROW_MAJOR),
                new CellDerivedItemIndex(items), CandidateScopes.of(ItemFilterConditionSpec.sameRow()),
                CellDerivedItemProvider.UNBOUNDED, CellDerivedProviderKind.UNRESTRICTED, true, false);
        assertEquals(List.of(), provider.provide(a));

        CellDerivedItem left = item(syntax, 1, 0, 0, ItemType.ATTRIBUTE);
        CellDerivedItem right = item(syntax, 1, 2, 0, ItemType.VALUE);
        items.add(right);
        items.add(left);
        assertEquals(List.of(left, right), provider.provide(a));
    }

    // --- reference implementation (the pre-optimisation algorithm) ---

    private static List<CellDerivedItem> reference(Set<CellDerivedItem> targetSet, CellDerivedItem anchor,
                                                   ItemFilterCondition kappa, TraversalOrder tau, int k,
                                                   CellDerivedProviderKind kind, boolean excludeAnchor) {
        boolean anchorOk = switch (kind) {
            case VAL, ATTR -> anchor.type() == ItemType.VALUE;
            case AUX -> anchor.type() != ItemType.AUXILIARY;
            default -> true;
        };
        if (!anchorOk) return null;
        Set<CellDerivedItem> candidates = new LinkedHashSet<>(targetSet);
        if (excludeAnchor) candidates.remove(anchor);
        candidates.removeIf(c -> switch (kind) {
            case VAL -> c.type() != ItemType.VALUE;
            case ATTR -> c.type() != ItemType.ATTRIBUTE;
            default -> false;
        });
        Set<CellDerivedItem> filtered = new ItemFilter(kappa).apply(anchor, candidates);
        List<CellDerivedItem> sorted = new ItemLinearization(tau).sort(filtered);
        return sorted.size() <= k ? sorted : sorted.subList(0, k);
    }

    // --- random fixtures ---

    private static TableSyntax randomStructure(Random rnd, int numRows, int numCols) {
        TableSyntax syntax = new TableSyntax(numRows, numCols);
        List<Integer> starts = new ArrayList<>(List.of(0));
        for (int r = 1; r < numRows; r++) if (rnd.nextInt(3) == 0) starts.add(r);
        syntax.defineSubtables(starts.stream().mapToInt(Integer::intValue).toArray());
        for (int r = 0; r < numRows; r++) {
            if (numCols > 1 && rnd.nextInt(2) == 0) {
                int split = 1 + rnd.nextInt(numCols - 1);
                syntax.defineSubrow(r, 0, split - 1);
                syntax.defineSubrow(r, split, numCols - 1);
            }
        }
        return syntax;
    }

    private static Set<CellDerivedItem> randomItems(Random rnd, TableSyntax syntax, int numRows, int numCols) {
        Set<CellDerivedItem> items = new LinkedHashSet<>();
        List<CellDerivedItem> shuffled = new ArrayList<>();
        ItemType[] types = ItemType.values();
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int n = rnd.nextInt(4);   // 0..3 items per cell
                for (int i = 0; i < n; i++) {
                    String s = rnd.nextInt(4) == 0 ? "" : "s" + rnd.nextInt(3);
                    List<String> tags = rnd.nextInt(3) == 0 ? List.of("#t") : List.of();
                    shuffled.add(new CellDerivedItem(s, tags, i, syntax.getCell(r, c), types[rnd.nextInt(types.length)]));
                }
            }
        }
        java.util.Collections.shuffle(shuffled, rnd);   // insertion order independent of position
        items.addAll(shuffled);
        return items;
    }

    private static CellDerivedItem item(TableSyntax syntax, int r, int c, int idx, ItemType type) {
        return new CellDerivedItem("x", idx, syntax.getCell(r, c), type);
    }

    private static List<ItemFilterConditionSpec> specs(Random rnd) {
        List<FilterTerm> atoms = List.of(
                FilterTerm.LeftOf.INSTANCE, FilterTerm.RightOf.INSTANCE,
                FilterTerm.Above.INSTANCE, FilterTerm.Below.INSTANCE,
                FilterTerm.SameSubrow.INSTANCE, FilterTerm.SameSubcol.INSTANCE, FilterTerm.SameSubtable.INSTANCE,
                FilterTerm.SameRow.INSTANCE, FilterTerm.SameCol.INSTANCE,
                FilterTerm.NotSameCell.INSTANCE, FilterTerm.SameCell.INSTANCE,
                new FilterTerm.ColExact(0), new FilterTerm.ColExact(2), new FilterTerm.ColExact(99),
                new FilterTerm.ColOffset(-1), new FilterTerm.ColOffset(0), new FilterTerm.ColOffset(1),
                new FilterTerm.ColRange(-1, 1), new FilterTerm.ColRange(1, Integer.MAX_VALUE),
                new FilterTerm.ColAbsoluteRange(1, 2), new FilterTerm.ColAbsoluteRange(1, Integer.MAX_VALUE),
                new FilterTerm.RowExact(0), new FilterTerm.RowExact(3), new FilterTerm.RowExact(99),
                new FilterTerm.RowOffset(-1), new FilterTerm.RowOffset(0), new FilterTerm.RowOffset(2),
                new FilterTerm.RowAbsoluteRange(0, 1), new FilterTerm.RowAbsoluteRange(2, Integer.MAX_VALUE),
                new FilterTerm.PosExact(0), new FilterTerm.PosOffset(1), new FilterTerm.PosRange(1, Integer.MAX_VALUE),
                new FilterTerm.RegexMatched("s1"), new FilterTerm.NotRegexMatched("s.*"),
                FilterTerm.Blank.INSTANCE, FilterTerm.NotBlank.INSTANCE,
                new FilterTerm.Tagged("#t"), FilterTerm.SameStr.INSTANCE);
        List<ItemFilterConditionSpec> specs = new ArrayList<>();
        for (FilterTerm t : atoms) specs.add(ItemFilterConditionSpec.bare(t));
        // typical RTL conjunctions
        specs.add(ItemFilterConditionSpec.and(FilterTerm.SameRow.INSTANCE, new FilterTerm.ColExact(0)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.SameCol.INSTANCE, new FilterTerm.RowExact(0)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.RightOf.INSTANCE, new FilterTerm.PosExact(0)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(1)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.RowOffset(-1)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColRange(0, 1)));
        specs.add(ItemFilterConditionSpec.and(FilterTerm.LeftOf.INSTANCE, FilterTerm.Above.INSTANCE));  // contradictory
        specs.add(ItemFilterConditionSpec.and(new FilterTerm.RowExact(0), new FilterTerm.RowOffset(-1)));
        specs.add(ItemFilterConditionSpec.and(new FilterTerm.RowAbsoluteRange(1, 3), new FilterTerm.ColOffset(0)));
        specs.add(ItemFilterConditionSpec.and(new FilterTerm.ColAbsoluteRange(0, 1), FilterTerm.SameSubtable.INSTANCE));
        // random conjunctions and a disjunction
        for (int i = 0; i < 12; i++) {
            specs.add(ItemFilterConditionSpec.and(atoms.get(rnd.nextInt(atoms.size())), atoms.get(rnd.nextInt(atoms.size()))));
        }
        specs.add(ItemFilterConditionSpec.or(
                ItemFilterConditionSpec.and(FilterTerm.SameRow.INSTANCE),
                ItemFilterConditionSpec.and(FilterTerm.SameCol.INSTANCE)));
        specs.add(ItemFilterConditionSpec.and());
        return specs;
    }

    private static String describe(ItemFilterConditionSpec spec) {
        try {
            return spec.toRtl();
        } catch (RuntimeException e) {
            return spec.toString();
        }
    }
}
