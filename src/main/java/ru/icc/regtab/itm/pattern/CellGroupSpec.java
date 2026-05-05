package ru.icc.regtab.itm.pattern;

import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.Cell;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Spec for a group of cells: count, whether to create value items, anchor, rec provider sequence.
 * cellCount: 1, n, or -1 for oneOrMore (skip)
 * <p>
 * When {@code compound} is non-null, {@code cellCount} is 1 and the cell text is split into
 * several logical value items per {@link CompoundSplitSpec}.
 * <p>
 * When {@code delimited} is non-null, {@code cellCount} is 1 and the cell text is split on
 * {@link DelimitedSplitSpec#delimiter()} into value items (empty trimmed segments are skipped).
 * <p>
 * {@code recProviders == null} means no rec action at group level. Non-null empty list is not used.
 * <p>
 * Optional {@code cellPredicate}: when non-null, used to match rows during subtable inference
 * and optional-row ({@code zeroOrOne}) handling. When null and group is skip, table application matches
 * any cell text (no value items are emitted); subtable boundary inference additionally treats plain
 * {@code skip()} as matching only blank cells. Use
 * {@code check(Cell::textBlank).skip()} to require blanks when matching rows during application too
 * (e.g. optional blank separator rows). When null and group has value, any cell text is accepted for matching.
 * <p>
 * {@code itemTags}: tags attached to value items from this group (for {@code rec} predicates).
 * <p>
 * Optional {@code branchWhen}: when non-null, each cell in this group is considered in isolation:
 * if {@code skipWhenBranchMatches == branchWhen.test(cell)}, no value item is emitted for that cell
 * (column position still advances). Otherwise behavior is as for a normal {@code val()} group.
 * <p>
 * For {@code cells().one().when(...).val().actions().fill/prefix/suffix(...).otherwise().val()}, {@code alwaysEmit} is true:
 * {@code branchWhen} only gates {@code O_fill} / {@code O_prefix} / {@code O_suffix}, and every cell still emits a value item.
 */
record CellGroupSpec(
        int cellCount,
        boolean hasVal,
        boolean isAnchor,
        List<ProviderSpec> recProviders,
        Predicate<Cell> cellPredicate,
        List<String> itemTags,
        CompoundSplitSpec compound,
        /** Emit {@link ru.icc.regtab.itm.model.semantics.item.ItemType#ATTRIBUTE} items from cell text. */
        boolean attributeItem,
        /** Emit {@link ru.icc.regtab.itm.model.semantics.item.ItemType#AUXILIARY} helper items (ITM auxiliary). Mutually exclusive with {@code attributeItem}. */
        boolean auxiliaryItem,
        /** If non-null, {@code A_avp} on each emitted item with this predicate (cell-derived provider). */
        ItemFilterCondition avpPredicate,
        /** If non-null, {@code A_avp} on anchor with a fixed context-derived attribute (e.g. Foofah ""). */
        String avpLiteralAttribute,
        /** If non-null, split cell text on this delimiter into multiple value items (mutually exclusive with compound). */
        DelimitedSplitSpec delimited,
        Predicate<Cell> branchWhen,
        boolean skipWhenBranchMatches,
        /** If non-null, emitted item string is {@code apply(cell)} (e.g. strip a fixed prefix). */
        Function<Cell, String> valueTextTransform,
        /** If non-null, {@code O_concat} on the rec anchor with this predicate (cell-derived provider). */
        ItemFilterCondition concatPredicate,
        /**
         * When {@code branchWhen} is set and this is true, skip logic is disabled and {@code branchWhen}
         * only controls {@link #fillSpec} / {@link #prefixSpec} / {@link #suffixSpec}
         * (see {@code one().when().val().fill().otherwise().val()}).
         */
        boolean alwaysEmit,
        /** If non-null, {@code O_fill} with the given provider sequence (see {@link FillSpec}). */
        FillSpec fillSpec,
        /** If non-null, {@code O_prefix} with the given provider sequence (see {@link PrefixSpec}). */
        PrefixSpec prefixSpec,
        /** If non-null, {@code O_suffix} with the given provider sequence (see {@link SuffixSpec}). */
        SuffixSpec suffixSpec
) {
    CellGroupSpec {
        if (attributeItem && auxiliaryItem) {
            throw new IllegalArgumentException("attributeItem and auxiliaryItem cannot both be true");
        }
    }

    boolean isSkip() {
        return !hasVal;
    }

    boolean hasRec() {
        if (compound != null) {
            return compound.tokens().stream()
                    .anyMatch(t -> t.recAnchor() && !t.recProviders().isEmpty());
        }
        if (delimited != null) {
            return recProviders != null && !recProviders.isEmpty();
        }
        return recProviders != null && !recProviders.isEmpty();
    }

    static CellGroupSpec withoutPredicate(int cellCount, boolean hasVal, boolean isAnchor,
                                          List<ProviderSpec> recProviders) {
        return new CellGroupSpec(cellCount, hasVal, isAnchor, recProviders, null, List.of(), null,
                false, false, null, null, null, null, false, null, null, false, null, null, null);
    }

    static CellGroupSpec compoundGroup(CompoundSplitSpec compound, Predicate<Cell> cellPredicate) {
        return new CellGroupSpec(1, true, false, null, cellPredicate, List.of(), compound,
                false, false, null, null, null, null, false, null, null, false, null, null, null);
    }

    static CellGroupSpec delimitedGroup(DelimitedSplitSpec delimited, Predicate<Cell> cellPredicate) {
        return new CellGroupSpec(1, true, false, null, cellPredicate, List.of(), null,
                false, false, null, null, delimited, null, false, null, null, false, null, null, null);
    }

    /**
     * {@code oneOrMore()} tail with per-cell branching ({@code when().skip().otherwise().val()}).
     */
    static CellGroupSpec conditionalValTail(int cellCount, Predicate<Cell> branchWhen,
                                            boolean skipWhenBranchMatches) {
        return new CellGroupSpec(cellCount, true, false, null, null, List.of(), null,
                false, false, null, null, null,
                Objects.requireNonNull(branchWhen, "branchWhen"), skipWhenBranchMatches, null, null, false, null, null, null);
    }

    /**
     * Fixed-width conditional group such as {@code cells().one().when(...).skip().otherwise().val()/attr()/aux()}.
     */
    static CellGroupSpec conditionalSingleCell(int cellCount,
                                               Predicate<Cell> branchWhen,
                                               boolean skipWhenBranchMatches,
                                               Predicate<Cell> cellPredicate,
                                               boolean attributeItem,
                                               boolean auxiliaryItem,
                                               Function<Cell, String> valueTextTransform,
                                               DelimitedSplitSpec delimited,
                                               CompoundSplitSpec compound) {
        return new CellGroupSpec(cellCount, true, false, null, cellPredicate, List.of(), compound,
                attributeItem, auxiliaryItem, null, null, delimited,
                Objects.requireNonNull(branchWhen, "branchWhen"), skipWhenBranchMatches,
                valueTextTransform, null, false, null, null, null);
    }

    /**
     * {@code cells().one().when(...).val()} before {@code actions().fill(...).otherwise().val()}.
     */
    static CellGroupSpec conditionalWhenFillBranch(Predicate<Cell> branchWhen, Predicate<Cell> cellPredicate) {
        return new CellGroupSpec(1, true, false, null, cellPredicate, List.of(), null,
                false, false, null, null, null,
                Objects.requireNonNull(branchWhen, "branchWhen"), false, null, null, true, null, null, null);
    }
}
