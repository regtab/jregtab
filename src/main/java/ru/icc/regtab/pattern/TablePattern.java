package ru.icc.regtab.pattern;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Entry point for the Table Pattern fluent API.
 * Declaratively describes table structure and generates InterpretableTable from TableSyntax.
 * Horizontal repetition within one row uses {@link RowCellsBuilder#subrows()} ({@code one()}, {@code oneOrMore()},
 * {@code exactly(n)}) after optional prefix {@link RowCellsBuilder#cells()} groups.
 * <p>
 * For {@code rec}, use {@link ActionsBuilder#rec()} for an anchor-only sequence, {@link ActionsBuilder#rec(ItemFilterCondition)}
 * for a single provider with ITM defaults (row-major traversal, unbounded cardinality), or
 * {@link ActionsBuilder#rec(ProviderSpec, ProviderSpec...)}
 * for several cell-derived providers. Per the ITM paper, working-state updates {@code O_suffix}, {@code O_prefix},
 * and {@code O_fill} (each with delimiter δ) are interpretation actions on the same anchor as {@code O_rec}.
 * {@link ActionsBuilder#fill}, {@link ActionsBuilder#prefix}, and {@link ActionsBuilder#suffix} share one implementation
 * path and return {@link AfterStringJoinBuilder}: on a normal cell use {@code .rec(...)} / {@code .cells()} like
 * {@link ActionsBuilder}; on {@code cells().one().when(...).val()} use {@link AfterStringJoinBuilder#otherwise}{@code .val()}.
 * Use {@link ActionsBuilder#avp(String)} before {@code rec} for {@code O_avp}.
 */
public final class TablePattern {

    /**
     * Predicate for {@link ActionsBuilder#rec()} / {@link CompoundValueActionsBuilder#rec()}: κ always false →
     * no tail items from {@code I_tbl} (rec sequence is {@code [anchor]} only).
     */
    private static final ItemFilterCondition REC_ANCHOR_ONLY = (a, c) -> false;

    private TablePattern() {}

    /**
     * Starts building a table pattern definition.
     */
    public static SubtablePatternBuilder define() {
        return new SubtablePatternBuilder();
    }

    /**
     * Builder for subtable-level pattern: {@link #subtables()} then {@link SubtableCardinalityBuilder}.
     */
    public static final class SubtablePatternBuilder {

        private final PatternDef patternDef = new PatternDef();

        /**
         * Subtable cardinality: {@code one()}, {@code oneOrMore()}, {@code exactly(n)} only (no {@code zeroOrOne} / {@code zeroOrMore}).
         */
        public SubtableCardinalityBuilder subtables() {
            return new SubtableCardinalityBuilder(patternDef);
        }
    }

    /**
     * After {@link SubtablePatternBuilder#subtables()}: only {@code one()}, {@code oneOrMore()}, {@code exactly(n)} —
     * same idea as {@link CellGroupCardinalityBuilder} after {@link RowCellsBuilder#cells()}.
     */
    public static final class SubtableCardinalityBuilder {

        private final PatternDef patternDef;

        SubtableCardinalityBuilder(PatternDef patternDef) {
            this.patternDef = patternDef;
        }

        /**
         * Exactly one subtable for the whole sheet: no stride or heuristic subtable boundaries are applied.
         */
        public RowsPatternBuilder one() {
            patternDef.setSubtableCountOne();
            return new RowsPatternBuilder(patternDef);
        }

        /**
         * One or more subtables: fixed row stride (when applicable) and/or heuristic boundaries may split the sheet.
         */
        public RowsPatternBuilder oneOrMore() {
            patternDef.setSubtableCountOneOrMore();
            return new RowsPatternBuilder(patternDef);
        }

        /**
         * Exactly {@code n} subtables after partitioning: inference must produce {@code n} contiguous blocks;
         * otherwise {@link PatternApplier} throws {@link IllegalStateException}.
         */
        public RowsPatternBuilder exactly(int n) {
            patternDef.setSubtableCountExactly(n);
            return new RowsPatternBuilder(patternDef);
        }
    }

    /**
     * Builder for repeating the row-pattern block within each subtable: only {@link #rows()}.
     */
    public static final class RowsPatternBuilder {

        private final PatternDef patternDef;

        RowsPatternBuilder(PatternDef patternDef) {
            this.patternDef = patternDef;
        }

        /**
         * Row-type cardinality per block: {@code one()}, {@code oneOrMore()}, {@code exactly(n)}, {@code zeroOrOne()},
         * {@code zeroOrMore()}.
         */
        public RowTypeCardinalityBuilder rows() {
            return new RowTypeCardinalityBuilder(patternDef);
        }
    }

    /**
     * After {@link RowsPatternBuilder#rows()}: row repetition (including optional / unbounded row types).
     */
    public static final class RowTypeCardinalityBuilder {

        private final PatternDef patternDef;

        RowTypeCardinalityBuilder(PatternDef patternDef) {
            this.patternDef = patternDef;
        }

        public RowCellsBuilder one() {
            int idx = patternDef.startRowType(1);
            return new RowCellsBuilder(patternDef, idx);
        }

        public RowCellsBuilder oneOrMore() {
            int idx = patternDef.startRowType(-1);
            return new RowCellsBuilder(patternDef, idx);
        }

        public RowCellsBuilder zeroOrOne() {
            int idx = patternDef.startRowType(2);
            return new RowCellsBuilder(patternDef, idx);
        }

        public RowCellsBuilder zeroOrMore() {
            int idx = patternDef.startRowType(3);
            return new RowCellsBuilder(patternDef, idx);
        }

        /**
         * Exactly {@code n} consecutive rows that match this row type (e.g. five continuation rows).
         */
        public RowCellsBuilder exactly(int n) {
            int idx = patternDef.startRowTypeExactly(n);
            return new RowCellsBuilder(patternDef, idx);
        }
    }

    /**
     * After prefix {@link RowCellsBuilder#cells()} groups: repeating horizontal segments within the same physical row.
     * Cardinality mirrors {@link RowTypeCardinalityBuilder}: {@link #one()}, {@link #oneOrMore()}, {@link #exactly(int)}.
     */
    public static final class SubrowsCardinalityBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;

        SubrowsCardinalityBuilder(PatternDef patternDef, int rowTypeIndex) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
        }

        /** Exactly one subrow segment after the prefix. */
        public RowCellsBuilder one() {
            patternDef.initSubrowsBlock(rowTypeIndex, 1, 0);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, true);
        }

        /** One or more subrow segments (until the row ends). */
        public RowCellsBuilder oneOrMore() {
            patternDef.initSubrowsBlock(rowTypeIndex, -1, 0);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, true);
        }

        /** Exactly {@code n} subrow segments. */
        public RowCellsBuilder exactly(int n) {
            if (n < 1) {
                throw new IllegalArgumentException("subrows().exactly(n) requires n >= 1, got: " + n);
            }
            patternDef.initSubrowsBlock(rowTypeIndex, 4, n);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, true);
        }
    }

    /**
     * After {@link RowCellsBuilder#cells()} or {@link ItemSpecBuilder#cells()}: only {@code one()},
     * {@code exactly(n)}, or {@code oneOrMore()} — not {@link RowCellsBuilder#rows()} or {@link RowCellsBuilder#apply(TableSyntax)}
     * (use {@link ItemSpecBuilder#rows()} / {@link ItemSpecBuilder#apply(TableSyntax)} or {@code actions().…}
     * {@link ActionsBuilder#rows()} / {@link ActionsBuilder#apply(TableSyntax)} when you need the next row type or to apply).
     */
    public static final class CellGroupCardinalityBuilder {

        private final RowCellsBuilder row;

        CellGroupCardinalityBuilder(RowCellsBuilder row) {
            this.row = row;
        }

        /** One cell. */
        public CellItemBuilder one() {
            return row.one();
        }

        /** Exactly {@code n} cells. */
        public CellItemBuilder exactly(int n) {
            return row.exactly(n);
        }

        /**
         * Remainder of the row: call {@link CellTailBuilder#skip()} or {@link CellTailBuilder#val()}.
         */
        public CellTailBuilder oneOrMore() {
            return row.oneOrMore();
        }
    }

    /**
     * Builder for cells within a row: cells().one(), cells().exactly(n), cells().oneOrMore().
     */
    public static final class RowCellsBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        /** Set after {@code one().when().val().actions().fill/prefix/suffix(...)} until {@link ConditionalOtherwiseCloser#val()}. */
        private final CellGroupSpec pendingConditionalGroup;
        /** When {@code true}, cell groups are appended to the current row type's {@code subrows} inner pattern. */
        private final boolean innerSubrows;

        RowCellsBuilder(PatternDef patternDef, int rowTypeIndex) {
            this(patternDef, rowTypeIndex, null, false);
        }

        RowCellsBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec pendingConditionalGroup) {
            this(patternDef, rowTypeIndex, pendingConditionalGroup, false);
        }

        RowCellsBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec pendingConditionalGroup, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.pendingConditionalGroup = pendingConditionalGroup;
            this.innerSubrows = innerSubrows;
        }

        private void ensureNoPendingConditional() {
            if (pendingConditionalGroup != null) {
                throw new IllegalStateException(
                        "Complete cells().one().when(...).val().actions().fill/prefix/suffix(...) with .otherwise().val() first");
            }
        }

        /**
         * Starts cell group definition: one(), exactly(n), oneOrMore().
         */
        public CellGroupCardinalityBuilder cells() {
            ensureNoPendingConditional();
            return new CellGroupCardinalityBuilder(this);
        }

        /**
         * Repeating horizontal segment(s) within this row type (after optional prefix {@link #cells()} groups).
         */
        public SubrowsCardinalityBuilder subrows() {
            ensureNoPendingConditional();
            return new SubrowsCardinalityBuilder(patternDef, rowTypeIndex);
        }

        private CellItemBuilder one() {
            ensureNoPendingConditional();
            return new CellItemBuilder(patternDef, rowTypeIndex, 1, null, innerSubrows);
        }

        private CellItemBuilder exactly(int n) {
            ensureNoPendingConditional();
            if (n < 1) throw new IllegalArgumentException("n must be positive: " + n);
            return new CellItemBuilder(patternDef, rowTypeIndex, n, null, innerSubrows);
        }

        private CellTailBuilder oneOrMore() {
            ensureNoPendingConditional();
            return new CellTailBuilder(patternDef, rowTypeIndex, innerSubrows);
        }

        /**
         * Defines subsequent rows of each subtable.
         */
        public RowTypeCardinalityBuilder rows() {
            ensureNoPendingConditional();
            return new RowTypeCardinalityBuilder(patternDef);
        }

        /**
         * Starts a new subtable pattern section, allowing multiple subtable definitions within the same pattern.
         * This is useful when different parts of the sheet have different subtable structures.
         */
        public SubtableCardinalityBuilder subtables() {
            ensureNoPendingConditional();
            return new SubtableCardinalityBuilder(patternDef);
        }

        /**
         * After {@code actions().rec(...)}, attaches context {@code O_avp} with this literal on the same anchor
         * (same meaning as {@link ActionsBuilder#avp(String)} before {@code rec}; order-independent).
         */
        public RowCellsBuilder avp(String literalAttribute) {
            ensureNoPendingConditional();
            patternDef.mergeAvpLiteralIntoLastRecGroup(rowTypeIndex, Objects.requireNonNull(literalAttribute, "literalAttribute"));
            return this;
        }

        /**
         * After {@code actions().rec(...)}, attaches {@code O_concat} on the same rec anchor (Foofah-style merge
         * of record sequences; same patch point as {@link #avp(String)} after {@code rec}).
         */
        public RowCellsBuilder concat(ItemFilterCondition concatPredicate) {
            ensureNoPendingConditional();
            patternDef.mergeConcatIntoLastRecGroup(rowTypeIndex, Objects.requireNonNull(concatPredicate, "concatPredicate"));
            return this;
        }

        /**
         * Closes {@code cells().one().when(...).val().actions().fill/prefix/suffix(...)}.
         */
        public ConditionalOtherwiseCloser otherwise() {
            if (pendingConditionalGroup == null) {
                throw new IllegalStateException(
                        "otherwise() only after cells().one().when(...).val().actions().fill/prefix/suffix(...)");
            }
            return new ConditionalOtherwiseCloser(patternDef, rowTypeIndex, pendingConditionalGroup, innerSubrows);
        }

        /**
         * Applies the pattern to the given TableSyntax and returns InterpretableTable.
         */
        public InterpretableTable apply(TableSyntax syntax) {
            ensureNoPendingConditional();
            return PatternApplier.apply(patternDef, syntax);
        }
    }

    /**
     * {@link RowCellsBuilder#otherwise()}{@code .val()}: commits the conditional single-cell group.
     */
    public static final class ConditionalOtherwiseCloser {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final CellGroupSpec spec;
        private final boolean innerSubrows;

        ConditionalOtherwiseCloser(PatternDef patternDef, int rowTypeIndex, CellGroupSpec spec, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.spec = spec;
            this.innerSubrows = innerSubrows;
        }

        public RowCellsBuilder val() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }
    }

    /**
     * Tail of a row after {@link RowCellsBuilder#oneOrMore()}: skip or value cells to end of row.
     */
    public static final class CellTailBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final boolean innerSubrows;

        CellTailBuilder(PatternDef patternDef, int rowTypeIndex, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.innerSubrows = innerSubrows;
        }

        /**
         * Consumes rest of row with no value items.
         */
        public RowCellsBuilder skip() {
            patternDef.addCellGroup(rowTypeIndex, -1, null, innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }

        /**
         * Predicate on each remaining cell; finish with {@link CellTailCheckBuilder#skip()}.
         */
        public CellTailCheckBuilder check(Predicate<Cell> predicate) {
            return new CellTailCheckBuilder(patternDef, rowTypeIndex,
                    Objects.requireNonNull(predicate, "predicate"), innerSubrows);
        }

        /**
         * One value item per remaining cell (e.g. wide header row).
         */
        public ItemSpecBuilder val() {
            CellGroupSpec spec = CellGroupSpec.withoutPredicate(-1, true, false, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Conditional tail: {@code when(pred).skip().otherwise().val()} skips emitting items for cells where
         * {@code pred} holds; other cells behave like {@link #val()}.
         */
        public WhenBuilder when(Predicate<Cell> predicate) {
            return new WhenBuilder(patternDef, rowTypeIndex, Objects.requireNonNull(predicate, "predicate"), innerSubrows);
        }
    }

    /**
     * After {@link CellTailBuilder#check(Predicate)}: {@link #skip()} consumes the rest of the row with no
     * value items; the predicate must hold for each cell (e.g. {@code check(Cell::textBlank).skip()}).
     */
    public static final class CellTailCheckBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> cellPredicate;
        private final boolean innerSubrows;

        CellTailCheckBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate) {
            this(patternDef, rowTypeIndex, cellPredicate, false);
        }

        CellTailCheckBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.cellPredicate = cellPredicate;
            this.innerSubrows = innerSubrows;
        }

        public RowCellsBuilder skip() {
            patternDef.addCellGroup(rowTypeIndex, -1,
                    new CellGroupSpec(-1, false, false, null, cellPredicate, List.of(), null,
                            false, false, null, null, null, null, false, null, null, false, null, null, null),
                    innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }
    }

    /**
     * After {@link CellTailBuilder#when(Predicate)}: call {@link #skip()}, then {@link OtherwiseBuilder#otherwise()}
     * and {@link OtherwiseBuilder#val()}.
     */
    public static final class WhenBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> branchWhen;
        private final boolean innerSubrows;

        WhenBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> branchWhen) {
            this(patternDef, rowTypeIndex, branchWhen, false);
        }

        WhenBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> branchWhen, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.branchWhen = branchWhen;
            this.innerSubrows = innerSubrows;
        }

        /**
         * When {@code branchWhen} matches a cell, do not emit a value item for it.
         */
        public OtherwiseBuilder skip() {
            return new OtherwiseBuilder(patternDef, rowTypeIndex, branchWhen, true, innerSubrows);
        }
    }

    /**
     * Non-skipped branch after {@link WhenBuilder#skip()}: {@link #otherwise()} is optional sugar;
     * then {@link #val()} as for a normal {@code oneOrMore().val()} group.
     */
    public static final class OtherwiseBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> branchWhen;
        private final boolean skipWhenBranchMatches;
        private final boolean innerSubrows;

        OtherwiseBuilder(PatternDef patternDef, int rowTypeIndex,
                         Predicate<Cell> branchWhen, boolean skipWhenBranchMatches) {
            this(patternDef, rowTypeIndex, branchWhen, skipWhenBranchMatches, false);
        }

        OtherwiseBuilder(PatternDef patternDef, int rowTypeIndex,
                         Predicate<Cell> branchWhen, boolean skipWhenBranchMatches, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.branchWhen = branchWhen;
            this.skipWhenBranchMatches = skipWhenBranchMatches;
            this.innerSubrows = innerSubrows;
        }

        public OtherwiseBuilder otherwise() {
            return this;
        }

        public ItemSpecBuilder val() {
            CellGroupSpec spec = CellGroupSpec.conditionalValTail(-1, branchWhen, skipWhenBranchMatches);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }
    }

    /**
     * Builder for item spec within a cell group: val(), anchor(), actions().rec().
     */
    public static final class CellItemBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final int cellCount;
        private final Predicate<Cell> cellPredicate;
        private final boolean innerSubrows;

        CellItemBuilder(PatternDef patternDef, int rowTypeIndex, int cellCount) {
            this(patternDef, rowTypeIndex, cellCount, null, false);
        }

        CellItemBuilder(PatternDef patternDef, int rowTypeIndex, int cellCount, Predicate<Cell> cellPredicate) {
            this(patternDef, rowTypeIndex, cellCount, cellPredicate, false);
        }

        CellItemBuilder(PatternDef patternDef, int rowTypeIndex, int cellCount, Predicate<Cell> cellPredicate,
                        boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.cellCount = cellCount;
            this.cellPredicate = cellPredicate;
            this.innerSubrows = innerSubrows;
        }

        /**
         * Predicate on each cell in this group (used for subtable inference and optional rows).
         */
        public CellItemBuilder check(Predicate<Cell> predicate) {
            return new CellItemBuilder(patternDef, rowTypeIndex, cellCount, Objects.requireNonNull(predicate, "predicate"),
                    innerSubrows);
        }

        /**
         * Value-associated item(s) in these cells.
         */
        public ItemSpecBuilder val() {
            CellGroupSpec spec = new CellGroupSpec(cellCount, true, false, null, cellPredicate, List.of(), null,
                    false, false, null, null, null, null, false, null, null, false, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Value item(s) with string from {@code valueTextTransform.apply(cell)} (e.g. strip a fixed prefix).
         */
        public ItemSpecBuilder val(Function<Cell, String> valueTextTransform) {
            Objects.requireNonNull(valueTextTransform, "valueTextTransform");
            CellGroupSpec spec = new CellGroupSpec(cellCount, true, false, null, cellPredicate, List.of(), null,
                    false, false, null, null, null, null, false, valueTextTransform, null, false, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Attribute-associated item(s): cell text becomes {@code ItemType.ATTRIBUTE} (for {@code A_avp}).
         */
        public ItemSpecBuilder attr() {
            CellGroupSpec spec = new CellGroupSpec(cellCount, true, false, null, cellPredicate, List.of(), null,
                    true, false, null, null, null, null, false, null, null, false, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Auxiliary helper item(s): cell text becomes {@link ru.icc.regtab.itm.semantics.item.ItemType#AUXILIARY} (ITM auxiliary / вспомогательные).
         */
        public ItemSpecBuilder aux() {
            CellGroupSpec spec = new CellGroupSpec(cellCount, true, false, null, cellPredicate, List.of(), null,
                    false, true, null, null, null, null, false, null, null, false, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Per-cell branch on one cell ({@code cells().one()} only):
         * {@code when(...).skip().otherwise().val()/attr()/...} or
         * {@code when(...).val().actions().fill/prefix/suffix(...).otherwise().val()}.
         */
        public SingleCellWhenBuilder when(Predicate<Cell> predicate) {
            if (cellCount != 1) {
                throw new IllegalStateException("when() requires cells().one()");
            }
            return new SingleCellWhenBuilder(patternDef, rowTypeIndex,
                    Objects.requireNonNull(predicate, "predicate"), cellPredicate, innerSubrows);
        }

        /**
         * One physical cell split into several logical items (see {@link CompoundBuilder}).
         */
        public CompoundBuilder compound() {
            if (cellCount != 1) {
                throw new IllegalStateException("compound() requires cells().one()");
            }
            return new CompoundBuilder(patternDef, rowTypeIndex, cellPredicate, innerSubrows);
        }

        /**
         * One cell split into several comma- or delimiter-separated value items; finish with {@link DelimitedBuilder#val()}.
         */
        public DelimitedBuilder delimited(String delimiter) {
            if (cellCount != 1) {
                throw new IllegalStateException("delimited() requires cells().one()");
            }
            return new DelimitedBuilder(patternDef, rowTypeIndex, cellPredicate,
                    Objects.requireNonNull(delimiter, "delimiter"), innerSubrows);
        }

        /**
         * Skip these cells (no value items created). Application-time row matching accepts any cell content
         * unless {@link #check(Predicate)} was used; subtable inference treats plain {@code skip()} as blank cells.
         * Use {@code check(Cell::textBlank).skip()} to require blanks in both phases (e.g. an optional blank row).
         */
        public RowCellsBuilder skip() {
            patternDef.addCellGroup(rowTypeIndex, cellCount,
                    new CellGroupSpec(cellCount, false, false, null, cellPredicate, List.of(), null,
                            false, false, null, null, null, null, false, null, null, false, null, null, null),
                    innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }
    }

    /**
     * After {@link CellItemBuilder#when(Predicate)}: either {@code skip().otherwise().val()/attr()/...}
     * or {@code val().actions().fill/prefix/suffix(...).otherwise().val()}.
     */
    public static final class SingleCellWhenBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> branchWhen;
        private final Predicate<Cell> cellPredicate;
        private final boolean innerSubrows;

        SingleCellWhenBuilder(PatternDef patternDef, int rowTypeIndex,
                              Predicate<Cell> branchWhen, Predicate<Cell> cellPredicate, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.branchWhen = branchWhen;
            this.cellPredicate = cellPredicate;
            this.innerSubrows = innerSubrows;
        }

        public SingleCellOtherwiseBuilder skip() {
            return new SingleCellOtherwiseBuilder(patternDef, rowTypeIndex,
                    branchWhen, true, cellPredicate, innerSubrows);
        }

        public ItemSpecBuilder val() {
            CellGroupSpec spec = CellGroupSpec.conditionalWhenFillBranch(branchWhen, cellPredicate);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }
    }

    /**
     * Non-skipped branch after {@link SingleCellWhenBuilder#skip()}.
     */
    public static final class SingleCellOtherwiseBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> branchWhen;
        private final boolean skipWhenBranchMatches;
        private final Predicate<Cell> cellPredicate;
        private final boolean innerSubrows;

        SingleCellOtherwiseBuilder(PatternDef patternDef, int rowTypeIndex,
                                   Predicate<Cell> branchWhen, boolean skipWhenBranchMatches,
                                   Predicate<Cell> cellPredicate, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.branchWhen = branchWhen;
            this.skipWhenBranchMatches = skipWhenBranchMatches;
            this.cellPredicate = cellPredicate;
            this.innerSubrows = innerSubrows;
        }

        public SingleCellOtherwiseBuilder otherwise() {
            return this;
        }

        public ItemSpecBuilder val() {
            CellGroupSpec spec = CellGroupSpec.conditionalSingleCell(
                    1, branchWhen, skipWhenBranchMatches, cellPredicate,
                    false, false, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        public ItemSpecBuilder val(Function<Cell, String> valueTextTransform) {
            Objects.requireNonNull(valueTextTransform, "valueTextTransform");
            CellGroupSpec spec = CellGroupSpec.conditionalSingleCell(
                    1, branchWhen, skipWhenBranchMatches, cellPredicate,
                    false, false, valueTextTransform, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        public CompoundBuilder attr() {
            return compound().attr();
        }

        public ItemSpecBuilder aux() {
            CellGroupSpec spec = CellGroupSpec.conditionalSingleCell(
                    1, branchWhen, skipWhenBranchMatches, cellPredicate,
                    false, true, null, null, null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        public CompoundBuilder compound() {
            return new CompoundBuilder(patternDef, rowTypeIndex, cellPredicate,
                    branchWhen, skipWhenBranchMatches, innerSubrows);
        }

        public DelimitedBuilder delimited(String delimiter) {
            Objects.requireNonNull(delimiter, "delimiter");
            return new DelimitedBuilder(patternDef, rowTypeIndex, cellPredicate,
                    branchWhen, skipWhenBranchMatches, delimiter, innerSubrows);
        }
    }

    /**
     * After {@link CellItemBuilder#delimited(String)}, call {@link #val()} then {@code actions().rec(...)} etc.
     */
    public static final class DelimitedBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> cellPredicate;
        private final Predicate<Cell> branchWhen;
        private final boolean skipWhenBranchMatches;
        private final String delimiter;
        private final boolean innerSubrows;

        DelimitedBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate, String delimiter,
                          boolean innerSubrows) {
            this(patternDef, rowTypeIndex, cellPredicate, null, false, delimiter, innerSubrows);
        }

        DelimitedBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate,
                         Predicate<Cell> branchWhen, boolean skipWhenBranchMatches, String delimiter,
                         boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.cellPredicate = cellPredicate;
            this.branchWhen = branchWhen;
            this.skipWhenBranchMatches = skipWhenBranchMatches;
            this.delimiter = delimiter;
            this.innerSubrows = innerSubrows;
        }

        public ItemSpecBuilder val() {
            CellGroupSpec spec = branchWhen == null
                    ? CellGroupSpec.delimitedGroup(new DelimitedSplitSpec(delimiter), cellPredicate)
                    : CellGroupSpec.conditionalSingleCell(
                    1, branchWhen, skipWhenBranchMatches, cellPredicate,
                    false, false, null, new DelimitedSplitSpec(delimiter), null);
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }
    }

    /**
     * Defines a single cell as {@code attr sep val} (or multiple value tokens).
     * {@link #val()} starts a value token: finish with {@link CompoundValueBuilder#sep(String)} (plain value),
     * {@link CompoundValueBuilder#actions()}{@code .rec(...)} (rec anchor), or end the compound with {@link #end()},
     * {@link #cells()}, {@link #apply(TableSyntax)}, {@link #rows()}, {@link #actions()}, etc. (same as {@link ItemSpecBuilder}
     * after the group is fixed).
     * <p>
     * Call {@link #sep(String)} between each pair of tokens (separators may differ). Use {@link #skip()} for a trailing
     * segment that is matched but emits no item.
     */
    public static final class CompoundBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final Predicate<Cell> cellPredicate;
        private final boolean innerSubrows;
        private final Predicate<Cell> branchWhen;
        private final boolean skipWhenBranchMatches;
        private final List<String> separators = new ArrayList<>();
        private final List<CompoundTokenSpec> tokens = new ArrayList<>();
        private CompoundValueBuilder pendingVal;
        private ItemFilterCondition avpPredicate;
        private String avpLiteralAttribute;

        CompoundBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate, boolean innerSubrows) {
            this(patternDef, rowTypeIndex, cellPredicate, null, false, innerSubrows);
        }

        CompoundBuilder(PatternDef patternDef, int rowTypeIndex, Predicate<Cell> cellPredicate,
                        Predicate<Cell> branchWhen, boolean skipWhenBranchMatches, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.cellPredicate = cellPredicate;
            this.branchWhen = branchWhen;
            this.skipWhenBranchMatches = skipWhenBranchMatches;
            this.innerSubrows = innerSubrows;
        }

        /** Attribute token (cell substring becomes {@link ru.icc.regtab.itm.semantics.item.ItemType#ATTRIBUTE}). */
        public CompoundBuilder attr() {
            ensureNoPendingVal("attr()");
            tokens.add(CompoundTokenSpec.attributePlain());
            return this;
        }

        /**
         * Starts a value token; complete with {@link CompoundValueBuilder#sep(String)},
         * {@link CompoundValueBuilder#actions()}{@code .rec(...)}, or {@link CompoundValueBuilder#end()} / {@link #end()} / {@link #apply(TableSyntax)}, …
         */
        public CompoundValueBuilder val() {
            if (pendingVal != null) {
                throw new IllegalStateException("Complete val() with sep(), actions().rec(), or end compound (end(), apply(), …)");
            }
            pendingVal = new CompoundValueBuilder(this);
            return pendingVal;
        }

        /** Literal separator between the previous token and the next one (each gap may use a different string). */
        public CompoundBuilder sep(String sep) {
            ensureNoPendingVal("sep(...)");
            Objects.requireNonNull(sep, "sep");
            if (sep.isEmpty()) {
                throw new IllegalArgumentException("sep must be non-empty");
            }
            separators.add(sep);
            return this;
        }

        /**
         * Trailing segment after {@link #sep(String)}: text is consumed for matching but no item is emitted.
         * Completes the compound cell (same as {@link #end()} / {@link #cells()} after a final value token).
         */
        public ItemSpecBuilder skip() {
            ensureNoPendingVal("skip()");
            tokens.add(CompoundTokenSpec.skip());
            return leaveCompound();
        }

        /**
         * Finishes the compound definition and returns {@link ItemSpecBuilder} (alias of {@link #cells()}).
         */
        public ItemSpecBuilder end() {
            return leaveCompound();
        }

        /**
         * Completes the compound cell and returns the same builder stage as {@link CellItemBuilder#val()},
         * so {@code actions().avp(...)} / {@link ItemSpecBuilder#cells()} can be used. Call {@link ItemSpecBuilder#cells()}
         * to commit the group when no actions are needed.
         */
        public ItemSpecBuilder cells() {
            return leaveCompound();
        }

        public InterpretableTable apply(TableSyntax syntax) {
            return leaveCompound().apply(syntax);
        }

        public RowTypeCardinalityBuilder rows() {
            return leaveCompound().rows();
        }

        public ItemSpecBuilder anchor() {
            return leaveCompound().anchor();
        }

        public ItemSpecBuilder setTag(String tag) {
            return leaveCompound().setTag(tag);
        }

        public ActionsBuilder actions() {
            return leaveCompound().actions();
        }

        public SubrowsCardinalityBuilder subrows() {
            return leaveCompound().subrows();
        }

        private ItemSpecBuilder leaveCompound() {
            if (pendingVal != null) {
                commitPlainToken();
            }
            return finishCompound();
        }

        private ItemSpecBuilder finishCompound() {
            if (tokens.isEmpty()) {
                throw new IllegalStateException("compound(): need at least val() or attr()");
            }
            if (separators.size() != tokens.size() - 1) {
                throw new IllegalStateException(
                        "compound(): expected " + (tokens.size() - 1) + " sep() between tokens, got " + separators.size());
            }
            CompoundSplitSpec cs = new CompoundSplitSpec(List.copyOf(separators), List.copyOf(tokens));
            CellGroupSpec baseSpec = branchWhen == null
                    ? CellGroupSpec.compoundGroup(cs, cellPredicate)
                    : CellGroupSpec.conditionalSingleCell(
                    1, branchWhen, skipWhenBranchMatches, cellPredicate,
                    false, false, null, null, cs);
            CellGroupSpec spec = new CellGroupSpec(
                    baseSpec.cellCount(),
                    baseSpec.hasVal(),
                    baseSpec.isAnchor(),
                    baseSpec.recProviders(),
                    baseSpec.cellPredicate(),
                    baseSpec.itemTags(),
                    baseSpec.compound(),
                    baseSpec.attributeItem(),
                    baseSpec.auxiliaryItem(),
                    avpPredicate,
                    avpLiteralAttribute,
                    baseSpec.delimited(),
                    baseSpec.branchWhen(),
                    baseSpec.skipWhenBranchMatches(),
                    baseSpec.valueTextTransform(),
                    baseSpec.concatPredicate(),
                    baseSpec.alwaysEmit(),
                    baseSpec.fillSpec(),
                    baseSpec.prefixSpec(),
                    baseSpec.suffixSpec());
            return new ItemSpecBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        private void ensureNoPendingVal(String op) {
            if (pendingVal != null) {
                throw new IllegalStateException("Complete val() before " + op);
            }
        }

        void commitPlainToken() {
            tokens.add(CompoundTokenSpec.plain());
            pendingVal = null;
        }

        void commitRecToken(List<ProviderSpec> providers) {
            tokens.add(CompoundTokenSpec.anchorRec(providers));
            pendingVal = null;
        }

        void commitAvpToken(ItemFilterCondition predicate) {
            if (avpLiteralAttribute != null) {
                throw new IllegalStateException("avp(literal) already set; cannot add avp(predicate) on the same compound token");
            }
            this.avpPredicate = predicate;
            // Don't set pendingVal to null - the token still needs to be committed
        }

        void commitAvpLiteralToken(String literalAttribute) {
            if (avpPredicate != null) {
                throw new IllegalStateException("avp(predicate) already set; cannot add avp(literal) on the same compound token");
            }
            this.avpLiteralAttribute = literalAttribute;
            // Don't set pendingVal to null - the token still needs to be committed
        }
    }

    /**
     * Stage after {@link CompoundBuilder#val()}: plain value ends with {@link #sep(String)}, or finish the compound with
     * {@link #end()}, {@link #cells()}, {@link #apply(TableSyntax)}, {@link #rows()}, {@link #anchor()}, {@link #setTag(String)},
     * {@link #subrows()} (same as {@link CompoundBuilder}).
     */
    public static final class CompoundValueBuilder {

        private final CompoundBuilder parent;

        CompoundValueBuilder(CompoundBuilder parent) {
            this.parent = parent;
        }

        /** Plain value token, then delimiter before the next token. */
        public CompoundBuilder sep(String sep) {
            parent.commitPlainToken();
            return parent.sep(sep);
        }

        /** {@code O_rec} on this value token (rec anchor). */
        public CompoundValueActionsBuilder actions() {
            return new CompoundValueActionsBuilder(parent);
        }

        /** Finishes the compound (alias of {@link #cells()}). */
        public ItemSpecBuilder end() {
            return parent.end();
        }

        /** Plain value token, then {@link CompoundBuilder#cells()} to finish the compound cell. */
        public ItemSpecBuilder cells() {
            return parent.cells();
        }

        public InterpretableTable apply(TableSyntax syntax) {
            return parent.apply(syntax);
        }

        public RowTypeCardinalityBuilder rows() {
            return parent.rows();
        }

        public ItemSpecBuilder anchor() {
            return parent.anchor();
        }

        public ItemSpecBuilder setTag(String tag) {
            return parent.setTag(tag);
        }

        public SubrowsCardinalityBuilder subrows() {
            return parent.subrows();
        }
    }

    /** {@code rec} for the current compound value token. */
    public static final class CompoundValueActionsBuilder {

        private final CompoundBuilder parent;

        CompoundValueActionsBuilder(CompoundBuilder parent) {
            this.parent = parent;
        }



        /**
         * {@code O_avp} with a predicate for the current compound value token.
         * The predicate selects the attribute cell.
         */
        public CompoundValueActionsBuilder avp(ItemFilterCondition predicate) {
            parent.commitAvpToken(Objects.requireNonNull(predicate, "predicate"));
            return this;
        }

        /**
         * {@code O_avp} with a fixed literal attribute for the current compound value token.
         */
        public CompoundValueActionsBuilder avp(String literalAttribute) {
            parent.commitAvpLiteralToken(Objects.requireNonNull(literalAttribute, "literalAttribute"));
            return this;
        }

        /**
         * Defines subsequent rows of each subtable after the current compound value token.
         */
        public RowTypeCardinalityBuilder rows() {
            // Commit the current value token if not already committed
            if (parent.pendingVal != null) {
                parent.commitPlainToken();
            }
            // Finish the compound and return to row level
            return parent.finishCompound().rows();
        }

        /**
         * Starts a new subtable pattern section after the current compound value token.
         */
        public SubtableCardinalityBuilder subtables() {
            // Commit the current value token if not already committed
            if (parent.pendingVal != null) {
                parent.commitPlainToken();
            }
            // Finish the compound and start new subtable section
            return parent.finishCompound().subtables();
        }

        /**
         * Compound {@code rec}: anchor may be value or attribute token; uses unrestricted J (legacy {@link ProviderSpec#of})
         * so attribute anchors are valid.
         */
        public CompoundBuilder rec(ItemFilterCondition predicate) {
            return rec(ProviderSpec.any(Objects.requireNonNull(predicate, "predicate")));
        }

        public CompoundBuilder rec(ProviderSpec first, ProviderSpec... rest) {
            List<ProviderSpec> list = new ArrayList<>();
            list.add(first);
            list.addAll(Arrays.asList(rest));
            parent.commitRecToken(List.copyOf(list));
            return parent;
        }

        /** Compound {@code rec} with anchor-only sequence (same as {@link #rec(ItemFilterCondition)} with a never-matching κ). */
        public CompoundBuilder rec() {
            return rec(REC_ANCHOR_ONLY);
        }

        /**
         * Commits the compound and applies the pattern.
         */
        public InterpretableTable apply(TableSyntax syntax) {
            return parent.apply(syntax);
        }
    }

    /**
     * Builder for item spec: anchor(), actions().rec().
     */
    public static final class ItemSpecBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final CellGroupSpec spec;
        private final boolean innerSubrows;

        ItemSpecBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec spec) {
            this(patternDef, rowTypeIndex, spec, false);
        }

        ItemSpecBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec spec, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.spec = spec;
            this.innerSubrows = innerSubrows;
        }

        /**
         * Marks the first item in this group as anchor (for RecAction).
         */
        public ItemSpecBuilder anchor() {
            return new ItemSpecBuilder(patternDef, rowTypeIndex,
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), true, spec.recProviders(),
                            spec.cellPredicate(), spec.itemTags(), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), spec.avpPredicate(), spec.avpLiteralAttribute(), spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
        }

        /**
         * Attaches a tag to value items in this group (visible in {@code rec} via {@code cand.has.hasTag(...)}).
         */
        public ItemSpecBuilder setTag(String tag) {
            return new ItemSpecBuilder(patternDef, rowTypeIndex,
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), spec.isAnchor(), spec.recProviders(),
                            spec.cellPredicate(), List.of(tag), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), spec.avpPredicate(), spec.avpLiteralAttribute(), spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
        }

        /**
         * Defines actions for items in this group.
         */
        public ActionsBuilder actions() {
            return new ActionsBuilder(patternDef, rowTypeIndex, spec, innerSubrows);
        }

        /**
         * Commits this cell group and starts repeating horizontal segments ({@link SubrowsCardinalityBuilder})
         * on the same physical row (after an optional prefix).
         */
        public SubrowsCardinalityBuilder subrows() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new SubrowsCardinalityBuilder(patternDef, rowTypeIndex);
        }

        /**
         * Commits this cell group and starts the next cell-group cardinality (same as {@link RowCellsBuilder#cells()}).
         */
        public CellGroupCardinalityBuilder cells() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new CellGroupCardinalityBuilder(new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows));
        }

        /**
         * Commits this cell group and applies the pattern (no further {@link #cells()} needed for the last group).
         */
        public InterpretableTable apply(TableSyntax syntax) {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return PatternApplier.apply(patternDef, syntax);
        }

        /**
         * Commits this group and starts the next row type (without an extra {@link RowCellsBuilder#cells()} step).
         */
        public RowTypeCardinalityBuilder rows() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new RowTypeCardinalityBuilder(patternDef);
        }

        /**
         * Commits this group and starts a new subtable pattern section.
         */
        public SubtableCardinalityBuilder subtables() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new SubtableCardinalityBuilder(patternDef);
        }
    }

    /**
     * Continuation after {@link ActionsBuilder#fill}, {@link ActionsBuilder#prefix}, or {@link ActionsBuilder#suffix}
     * on the same anchor: on an ordinary value cell, delegates to {@link ActionsBuilder} for {@code rec} / {@code cells}
     * / {@code avp} / {@code rows} / {@code apply}; on {@code cells().one().when(...).val()}, delegates to
     * {@link RowCellsBuilder#otherwise}{@code .val()}.
     */
    public static final class AfterStringJoinBuilder {

        private final ActionsBuilder actions;
        private final RowCellsBuilder conditionalRow;

        AfterStringJoinBuilder(ActionsBuilder actions) {
            this.actions = Objects.requireNonNull(actions);
            this.conditionalRow = null;
        }

        AfterStringJoinBuilder(RowCellsBuilder conditionalRow) {
            this.actions = null;
            this.conditionalRow = Objects.requireNonNull(conditionalRow);
        }

        /** @see ActionsBuilder#avp(String) */
        public ActionsBuilder avp(String literalAttribute) {
            return requireActions().avp(literalAttribute);
        }

        /** @see ActionsBuilder#avp(ItemFilterCondition) */
        public ActionsBuilder avp(ItemFilterCondition predicate) {
            return requireActions().avp(predicate);
        }

        /** @see ActionsBuilder#rec() */
        public RowCellsBuilder rec() {
            return requireActions().rec();
        }

        /** @see ActionsBuilder#rec(ItemFilterCondition) */
        public RowCellsBuilder rec(ItemFilterCondition predicate) {
            return requireActions().rec(predicate);
        }

        /** @see ActionsBuilder#rec(ProviderSpec, ProviderSpec...) */
        public RowCellsBuilder rec(ProviderSpec first, ProviderSpec... rest) {
            return requireActions().rec(first, rest);
        }

        /** @see ActionsBuilder#cells() */
        public CellGroupCardinalityBuilder cells() {
            return requireActions().cells();
        }

        /** @see ActionsBuilder#rows() */
        public RowTypeCardinalityBuilder rows() {
            return requireActions().rows();
        }

        /** @see ActionsBuilder#apply(TableSyntax) */
        public InterpretableTable apply(TableSyntax syntax) {
            return requireActions().apply(syntax);
        }

        /**
         * Completes {@code cells().one().when(...).val().actions().fill(...)} / {@code prefix(...)} / {@code suffix(...)}.
         */
        public ConditionalOtherwiseCloser otherwise() {
            if (conditionalRow == null) {
                throw new IllegalStateException(
                        "otherwise() only after fill/prefix/suffix on cells().one().when(...).val()");
            }
            return conditionalRow.otherwise();
        }

        private ActionsBuilder requireActions() {
            if (actions == null) {
                throw new IllegalStateException(
                        "Use otherwise().val() after fill/prefix/suffix on when(...).val(); rec/cells/rows/apply/avp are not valid here");
            }
            return actions;
        }
    }

    /**
     * Builder for interpretive actions on the current cell group. In the ITM paper, several working-state updates
     * (e.g. {@code O_fill}, {@code O_prefix}, {@code O_suffix}, {@code O_rec}, {@code O_avp}) are separate
     * interpretation actions on the same anchor. {@link #fill}, {@link #prefix}, and {@link #suffix} return
     * {@link AfterStringJoinBuilder}; {@link #avp(String)} returns this builder so {@code rec} can follow.
     * After {@code rec}, use {@link RowCellsBuilder#avp(String)} / {@link RowCellsBuilder#concat} on the returned row
     * builder.
     */
    public static final class ActionsBuilder {

        private final PatternDef patternDef;
        private final int rowTypeIndex;
        private final CellGroupSpec spec;
        private final boolean innerSubrows;

        ActionsBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec spec) {
            this(patternDef, rowTypeIndex, spec, false);
        }

        ActionsBuilder(PatternDef patternDef, int rowTypeIndex, CellGroupSpec spec, boolean innerSubrows) {
            this.patternDef = patternDef;
            this.rowTypeIndex = rowTypeIndex;
            this.spec = spec;
            this.innerSubrows = innerSubrows;
        }

        /**
         * {@code O_avp} with a fixed context-derived attribute item (string becomes {@code attr(ι_1)} in the paper).
         * Returns this builder so you can chain {@link #rec(ItemFilterCondition)} on the same anchor.
         */
        public ActionsBuilder avp(String literalAttribute) {
            Objects.requireNonNull(literalAttribute, "literalAttribute");
            if (spec.avpPredicate() != null) {
                throw new IllegalStateException("avp(predicate) already set; cannot add avp(literal) on the same cell group");
            }
            return new ActionsBuilder(patternDef, rowTypeIndex,
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), spec.isAnchor(), spec.recProviders(),
                            spec.cellPredicate(), spec.itemTags(), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), null, literalAttribute, spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
        }

        /**
         * RecAction with one provider: row-major traversal, unbounded cardinality (ITM defaults for simple patterns).
         */
        public RowCellsBuilder rec(ItemFilterCondition predicate) {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(),
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), true,
                            List.of(ProviderSpec.val(predicate)), spec.cellPredicate(), spec.itemTags(), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), spec.avpPredicate(), spec.avpLiteralAttribute(), spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }

        /**
         * {@code O_rec} with no additional table items: {@code rec(anchor) := ⟨anchor⟩} (κ matches no candidate).
         */
        public RowCellsBuilder rec() {
            return rec(REC_ANCHOR_ONLY);
        }

        /**
         * {@code O_avp} on items in this group (anchor = value cell; provider picks attribute cell via predicate).
         * Chain {@link #rec(ItemFilterCondition)}, {@link #rec(ProviderSpec, ProviderSpec...)}, {@link #cells()}, or {@link #rows()} after.
         */
        public ActionsBuilder avp(ItemFilterCondition predicate) {
            if (spec.avpLiteralAttribute() != null) {
                throw new IllegalStateException("avp(literal) already set; use avp(predicate) on another cell group");
            }
            return new ActionsBuilder(patternDef, rowTypeIndex,
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), spec.isAnchor(), spec.recProviders(),
                            spec.cellPredicate(), spec.itemTags(), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), Objects.requireNonNull(predicate, "predicate"), null, spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
        }

        /**
         * {@code O_fill} with delimiter {@code ""} and the same defaults as {@link #rec(ItemFilterCondition)}
         * ({@link ProviderSpec#of(ItemFilterCondition)}).
         */
        public AfterStringJoinBuilder fill(ItemFilterCondition predicate) {
            return fill("", Objects.requireNonNull(predicate, "predicate"));
        }

        /**
         * {@code O_fill} with the same defaults as {@link #rec(ItemFilterCondition)} ({@link ProviderSpec#of(ItemFilterCondition)}).
         * Chain {@link AfterStringJoinBuilder#rec}, {@link AfterStringJoinBuilder#cells}, or (on
         * {@code when(...).val()}) {@link AfterStringJoinBuilder#otherwise}.
         */
        public AfterStringJoinBuilder fill(String delimiter, ItemFilterCondition predicate) {
            return fill(delimiter, ProviderSpec.aux(Objects.requireNonNull(predicate, "predicate")));
        }

        /**
         * {@code O_fill} with delimiter {@code ""} (same as {@link #fill(String, ProviderSpec, ProviderSpec...)}).
         */
        public AfterStringJoinBuilder fill(ProviderSpec first, ProviderSpec... more) {
            return fill("", first, more);
        }

        /**
         * {@code O_fill} with one or more cell-derived providers (same shape as {@link #rec(ProviderSpec, ProviderSpec...)}):
         * candidate sequences are concatenated; delimiter δ joins {@code str(ι)} (ITM).
         */
        public AfterStringJoinBuilder fill(String delimiter, ProviderSpec first, ProviderSpec... more) {
            Objects.requireNonNull(delimiter, "delimiter");
            Objects.requireNonNull(first, "first");
            List<ProviderSpec> list = new ArrayList<>(1 + more.length);
            list.add(first);
            list.addAll(Arrays.asList(more));
            return applyStringJoinAction(new FillSpec(list, delimiter), null, null);
        }

        /**
         * {@code O_prefix} with delimiter {@code ""} and {@link ProviderSpec#of(ItemFilterCondition)}.
         */
        public AfterStringJoinBuilder prefix(ItemFilterCondition predicate) {
            return prefix("", Objects.requireNonNull(predicate, "predicate"));
        }

        /**
         * {@code O_prefix} with the same defaults as {@link #rec(ItemFilterCondition)} ({@link ProviderSpec#of(ItemFilterCondition)}).
         */
        public AfterStringJoinBuilder prefix(String delimiter, ItemFilterCondition predicate) {
            return prefix(delimiter, ProviderSpec.aux(Objects.requireNonNull(predicate, "predicate")));
        }

        /**
         * {@code O_prefix} with delimiter {@code ""} (same as {@link #prefix(String, ProviderSpec, ProviderSpec...)}).
         */
        public AfterStringJoinBuilder prefix(ProviderSpec first, ProviderSpec... more) {
            return prefix("", first, more);
        }

        /**
         * {@code O_prefix}: prepend delimiter-joined {@code str(ι)} from one or more providers (ITM).
         */
        public AfterStringJoinBuilder prefix(String delimiter, ProviderSpec first, ProviderSpec... more) {
            Objects.requireNonNull(delimiter, "delimiter");
            Objects.requireNonNull(first, "first");
            List<ProviderSpec> list = new ArrayList<>(1 + more.length);
            list.add(first);
            list.addAll(Arrays.asList(more));
            return applyStringJoinAction(null, new PrefixSpec(list, delimiter), null);
        }

        /**
         * {@code O_suffix} with delimiter {@code ""} and {@link ProviderSpec#of(ItemFilterCondition)}.
         */
        public AfterStringJoinBuilder suffix(ItemFilterCondition predicate) {
            return suffix("", Objects.requireNonNull(predicate, "predicate"));
        }

        /**
         * {@code O_suffix} with the same defaults as {@link #rec(ItemFilterCondition)} ({@link ProviderSpec#of(ItemFilterCondition)}).
         */
        public AfterStringJoinBuilder suffix(String delimiter, ItemFilterCondition predicate) {
            return suffix(delimiter, ProviderSpec.aux(Objects.requireNonNull(predicate, "predicate")));
        }

        /**
         * {@code O_suffix} with delimiter {@code ""} (same as {@link #suffix(String, ProviderSpec, ProviderSpec...)}).
         */
        public AfterStringJoinBuilder suffix(ProviderSpec first, ProviderSpec... more) {
            return suffix("", first, more);
        }

        /**
         * {@code O_suffix}: append delimiter-joined {@code str(ι)} from one or more providers (ITM).
         */
        public AfterStringJoinBuilder suffix(String delimiter, ProviderSpec first, ProviderSpec... more) {
            Objects.requireNonNull(delimiter, "delimiter");
            Objects.requireNonNull(first, "first");
            List<ProviderSpec> list = new ArrayList<>(1 + more.length);
            list.add(first);
            list.addAll(Arrays.asList(more));
            return applyStringJoinAction(null, null, new SuffixSpec(list, delimiter));
        }

        /**
         * Shared path for {@code O_fill}, {@code O_prefix}, and {@code O_suffix}: merge into the pending cell spec,
         * then either defer to {@code otherwise().val()} (conditional single cell) or expose {@link ActionsBuilder}
         * continuations ({@code rec} / {@code cells} / …).
         */
        private AfterStringJoinBuilder applyStringJoinAction(FillSpec fill, PrefixSpec prefix, SuffixSpec suffix) {
            int n = (fill != null ? 1 : 0) + (prefix != null ? 1 : 0) + (suffix != null ? 1 : 0);
            if (n != 1) {
                throw new IllegalArgumentException("internal: exactly one of fill, prefix, suffix");
            }
            ensureNoStringJoinAction();
            CellGroupSpec merged = new CellGroupSpec(spec.cellCount(), spec.hasVal(), spec.isAnchor(), spec.recProviders(),
                    spec.cellPredicate(), spec.itemTags(), spec.compound(),
                    spec.attributeItem(), spec.auxiliaryItem(), spec.avpPredicate(), spec.avpLiteralAttribute(),
                    spec.delimited(),
                    spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                    spec.alwaysEmit(),
                    fill, prefix, suffix);
            if (spec.alwaysEmit() && spec.branchWhen() != null) {
                return new AfterStringJoinBuilder(new RowCellsBuilder(patternDef, rowTypeIndex, merged, innerSubrows));
            }
            return new AfterStringJoinBuilder(new ActionsBuilder(patternDef, rowTypeIndex, merged, innerSubrows));
        }

        private void ensureNoStringJoinAction() {
            if (spec.fillSpec() != null || spec.prefixSpec() != null || spec.suffixSpec() != null) {
                throw new IllegalStateException("fill, prefix, or suffix already set for this cell group");
            }
        }

        /**
         * Commits this cell group after {@link #avp(String)} when no {@code rec} is needed; next step is cell-group cardinality
         * (same as {@link ItemSpecBuilder#cells()}).
         */
        public CellGroupCardinalityBuilder cells() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new CellGroupCardinalityBuilder(new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows));
        }

        /**
         * Commits this group and starts the next row type (same as {@link ItemSpecBuilder#rows()}).
         */
        public RowTypeCardinalityBuilder rows() {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return new RowTypeCardinalityBuilder(patternDef);
        }

        /**
         * Commits this cell group and applies the pattern (no further {@link #cells()} for the last group).
         */
        public InterpretableTable apply(TableSyntax syntax) {
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(), spec, innerSubrows);
            return PatternApplier.apply(patternDef, syntax);
        }

        /**
         * RecAction with one or more providers (each with its own predicate, traversal, and cardinality).
         */
        public RowCellsBuilder rec(ProviderSpec first, ProviderSpec... rest) {
            List<ProviderSpec> list = new ArrayList<>(rest.length + 1);
            list.add(first);
            list.addAll(Arrays.asList(rest));
            patternDef.addCellGroup(rowTypeIndex, spec.cellCount(),
                    new CellGroupSpec(spec.cellCount(), spec.hasVal(), true, List.copyOf(list),
                            spec.cellPredicate(), spec.itemTags(), spec.compound(),
                            spec.attributeItem(), spec.auxiliaryItem(), spec.avpPredicate(), spec.avpLiteralAttribute(), spec.delimited(),
                            spec.branchWhen(), spec.skipWhenBranchMatches(), spec.valueTextTransform(), spec.concatPredicate(),
                            spec.alwaysEmit(),
                            spec.fillSpec(), spec.prefixSpec(), spec.suffixSpec()),
                    innerSubrows);
            return new RowCellsBuilder(patternDef, rowTypeIndex, null, innerSubrows);
        }
    }
}
