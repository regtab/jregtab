package ru.icc.regtab.pattern;

import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Internal: collects the pattern definition (cell groups per row type).
 * Supports multiple row types: rows().one() adds a row type with exactly 1 row,
 * rows().oneOrMore() adds a row type with 1+ rows.
 */
final class PatternDef {

    private final List<RowTypeDef> rowTypes = new ArrayList<>();

    /**
     * How many subtables the pattern allows on the sheet (see {@link ru.icc.regtab.pattern.TablePattern.SubtableCardinalityBuilder}).
     */
    enum SubtableCountKind {
        /** Sheet is one contiguous subtable; inference does not insert boundaries. */
        ONE,
        /** Default: stride and/or heuristic boundaries may split into one or more subtables. */
        ONE_OR_MORE,
        /** Partition must yield exactly {@link #exactSubtableCount} subtables (validated after inference). */
        EXACTLY
    }

    private SubtableCountKind subtableCountKind = SubtableCountKind.ONE_OR_MORE;
    /** Used when {@link #subtableCountKind} is {@link SubtableCountKind#EXACTLY}. */
    private int exactSubtableCount = 1;

    void setSubtableCountOne() {
        this.subtableCountKind = SubtableCountKind.ONE;
    }

    void setSubtableCountOneOrMore() {
        this.subtableCountKind = SubtableCountKind.ONE_OR_MORE;
    }

    void setSubtableCountExactly(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("subtables().exactly(n) requires n >= 1, got: " + n);
        }
        this.subtableCountKind = SubtableCountKind.EXACTLY;
        this.exactSubtableCount = n;
    }

    SubtableCountKind subtableCountKind() {
        return subtableCountKind;
    }

    int exactSubtableCount() {
        return exactSubtableCount;
    }

    /**
     * Rows per subtable when boundaries are a fixed stride: {@code rows().exactly(n)} alone → {@code n};
     * or the sum of row counts in a sequence of only fixed row types — each {@code rows().one()} contributes
     * {@code 1}, each {@code rows().exactly(k)} contributes {@code k}. If any row type is variable
     * ({@code oneOrMore}, {@code zeroOrOne}, …), returns {@code 0} so stride-based partitioning is not used.
     */
    int effectiveRowsPerSubtable() {
        int sum = 0;
        for (RowTypeDef rt : rowTypes) {
            if (rt.cardinality == 1) {
                sum += 1;
            } else if (rt.cardinality == 4 && rt.exactCount > 0) {
                sum += rt.exactCount;
            } else {
                return 0;
            }
        }
        return sum;
    }

    int startRowType(int cardinality) {
        rowTypes.add(new RowTypeDef(cardinality, 0));
        return rowTypes.size() - 1;
    }

    /** Cardinality 4 = exactly {@code n} consecutive rows matching this row type. */
    int startRowTypeExactly(int n) {
        if (n < 1) throw new IllegalArgumentException("n must be positive: " + n);
        rowTypes.add(new RowTypeDef(4, n));
        return rowTypes.size() - 1;
    }

    void addCellGroup(int rowTypeIndex, int cellCount, CellGroupSpec spec) {
        addCellGroup(rowTypeIndex, cellCount, spec, false);
    }

    /**
     * @param innerSubrows if {@code true}, append to the current row type's {@link SubrowsBlock#innerGroups}
     *                     (after {@code subrows()...}); otherwise append to the prefix {@link RowTypeDef#groups}.
     */
    void addCellGroup(int rowTypeIndex, int cellCount, CellGroupSpec spec, boolean innerSubrows) {
        CellGroupSpec toAdd = spec != null ? spec : CellGroupSpec.withoutPredicate(cellCount, false, false, null);
        RowTypeDef rt = rowTypes.get(rowTypeIndex);
        if (innerSubrows) {
            if (rt.subrowsBlock == null) {
                throw new IllegalStateException("Call subrows().one|oneOrMore|exactly(n) before inner cells()");
            }
            validateSubrowInnerGroupWidth(toAdd);
            rt.subrowsBlock.innerGroups.add(toAdd);
        } else {
            if (rt.subrowsBlock != null) {
                throw new IllegalStateException("Prefix cell groups must be declared before subrows()");
            }
            rt.groups.add(toAdd);
        }
    }

    /**
     * Declares a repeating horizontal segment within the current row type (after optional prefix groups).
     */
    void initSubrowsBlock(int rowTypeIndex, int cardinality, int exactCount) {
        RowTypeDef rt = rowTypes.get(rowTypeIndex);
        if (rt.subrowsBlock != null) {
            throw new IllegalStateException("subrows() already set for this row type");
        }
        rt.subrowsBlock = new SubrowsBlock(cardinality, exactCount);
    }

    private static void validateSubrowInnerGroupWidth(CellGroupSpec spec) {
        if (spec.cellCount() == -1) {
            throw new IllegalStateException(
                    "subrows inner pattern cannot end with cells().oneOrMore() tail; use fixed-width groups");
        }
    }

    /**
     * For {@code rec(...).avp("")}: attach context {@code O_avp} literal to the last cell group in this row type
     * that has {@code rec} but no {@link CellGroupSpec#avpLiteralAttribute()} yet (search from end).
     */
    void mergeAvpLiteralIntoLastRecGroup(int rowTypeIndex, String literal) {
        RowTypeDef rt = rowTypes.get(rowTypeIndex);
        if (mergeAvpLiteralIntoLastRecGroupInList(rt.groups, literal)) {
            return;
        }
        if (rt.subrowsBlock != null && mergeAvpLiteralIntoLastRecGroupInList(rt.subrowsBlock.innerGroups, literal)) {
            return;
        }
        throw new IllegalStateException(
                "No cell group with rec and without context avp literal to patch in row type " + rowTypeIndex);
    }

    private static boolean mergeAvpLiteralIntoLastRecGroupInList(List<CellGroupSpec> groups, String literal) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            CellGroupSpec g = groups.get(i);
            if (g.hasRec() && g.avpLiteralAttribute() == null) {
                groups.set(i, new CellGroupSpec(
                        g.cellCount(), g.hasVal(), g.isAnchor(), g.recProviders(),
                        g.cellPredicate(), g.itemTags(), g.compound(),
                        g.attributeItem(), g.auxiliaryItem(), g.avpPredicate(), literal, g.delimited(),
                        g.branchWhen(), g.skipWhenBranchMatches(), g.valueTextTransform(),
                        g.concatPredicate(),
                        g.alwaysEmit(),
                        g.fillSpec(), g.prefixSpec(), g.suffixSpec()));
                return true;
            }
        }
        return false;
    }

    /**
     * For {@code rec(...).concat(…)}: attach {@code O_concat} predicate to the last cell group in this row type
     * that has {@code rec} but no {@link CellGroupSpec#concatPredicate()} yet (search from end).
     */
    void mergeConcatIntoLastRecGroup(int rowTypeIndex, ItemFilterCondition concatPredicate) {
        RowTypeDef rt = rowTypes.get(rowTypeIndex);
        if (mergeConcatIntoLastRecGroupInList(rt.groups, concatPredicate)) {
            return;
        }
        if (rt.subrowsBlock != null && mergeConcatIntoLastRecGroupInList(rt.subrowsBlock.innerGroups, concatPredicate)) {
            return;
        }
        throw new IllegalStateException(
                "No cell group with rec and without concat predicate to patch in row type " + rowTypeIndex);
    }

    private static boolean mergeConcatIntoLastRecGroupInList(List<CellGroupSpec> groups, ItemFilterCondition concatPredicate) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            CellGroupSpec g = groups.get(i);
            if (g.hasRec() && g.concatPredicate() == null) {
                groups.set(i, new CellGroupSpec(
                        g.cellCount(), g.hasVal(), g.isAnchor(), g.recProviders(),
                        g.cellPredicate(), g.itemTags(), g.compound(),
                        g.attributeItem(), g.auxiliaryItem(), g.avpPredicate(), g.avpLiteralAttribute(), g.delimited(),
                        g.branchWhen(), g.skipWhenBranchMatches(), g.valueTextTransform(),
                        Objects.requireNonNull(concatPredicate, "concatPredicate"),
                        g.alwaysEmit(),
                        g.fillSpec(), g.prefixSpec(), g.suffixSpec()));
                return true;
            }
        }
        return false;
    }

    List<RowTypeDef> rowTypes() {
        return List.copyOf(rowTypes);
    }

    static final class RowTypeDef {
        /** 1 = one, -1 = oneOrMore, 2 = zeroOrOne, 3 = zeroOrMore, 4 = exactly(exactCount) */
        final int cardinality;
        /** Used when {@code cardinality == 4}. */
        final int exactCount;
        /** Prefix cell groups (left part of the row) before an optional {@link #subrowsBlock}. */
        final List<CellGroupSpec> groups = new ArrayList<>();
        /**
         * Repeating horizontal segment: same inner cell groups repeated {@code cardinality} times
         * ({@code one()}, {@code oneOrMore()}, {@code exactly(n)}).
         */
        SubrowsBlock subrowsBlock;

        RowTypeDef(int cardinality, int exactCount) {
            this.cardinality = cardinality;
            this.exactCount = exactCount;
        }
    }

    /** Inner pattern repeated horizontally within one physical row (see {@link RowTypeDef#subrowsBlock}). */
    static final class SubrowsBlock {
        /** 1 = one, -1 = oneOrMore, 4 = exactly(exactCount) */
        final int cardinality;
        final int exactCount;
        final List<CellGroupSpec> innerGroups = new ArrayList<>();

        SubrowsBlock(int cardinality, int exactCount) {
            this.cardinality = cardinality;
            this.exactCount = exactCount;
        }
    }
}
