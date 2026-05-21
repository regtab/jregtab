package ru.icc.regtab.pattern;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.operation.AvpOperation;
import ru.icc.regtab.itm.semantics.operation.ConcatOperation;
import ru.icc.regtab.itm.semantics.operation.FillOperation;
import ru.icc.regtab.itm.semantics.operation.PrefixOperation;
import ru.icc.regtab.itm.semantics.operation.RecOperation;
import ru.icc.regtab.itm.semantics.operation.SuffixOperation;
import ru.icc.regtab.itm.semantics.provider.CellDerivedItemProvider;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.semantics.provider.ContextDerivedItemProvider;
import ru.icc.regtab.itm.semantics.provider.ContextDerivedProviderKind;
import ru.icc.regtab.itm.semantics.provider.ItemProvider;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.Row;
import ru.icc.regtab.itm.syntax.Subtable;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Applies a PatternDef to TableSyntax and produces InterpretableTable.
 * Respects {@link PatternDef#subtableCountKind()}: {@code subtables().one()} forces a single subtable;
 * {@code oneOrMore()} uses stride and/or heuristic boundaries; {@code exactly(n)} validates the
 * inferred partition count.
 */
final class PatternApplier {

    static InterpretableTable apply(PatternDef patternDef, TableSyntax syntax) {
        Set<CellDerivedItem> allCdi = new LinkedHashSet<>();
        Set<ContextDerivedItem> contextItems = new LinkedHashSet<>();
        List<InterpretationAction> actions = new ArrayList<>();

        List<PatternDef.RowTypeDef> rowTypes = patternDef.rowTypes();
        if (rowTypes.isEmpty()) {
            TableSemantics semantics = new TableSemantics(allCdi, contextItems, List.of());
            return new InterpretableTable(syntax, semantics);
        }

        inferAndApplySubtables(patternDef, syntax);

        int repeatedTailStart = repeatedTailRowTypeStart(patternDef, syntax);
        int subtableOrdinal = 0;
        for (Subtable subtable : syntax.subtables()) {
            List<PatternDef.RowTypeDef> activeRowTypes = rowTypes;
            if (repeatedTailStart >= 0 && subtableOrdinal > 0) {
                activeRowTypes = rowTypes.subList(repeatedTailStart, rowTypes.size());
            }
            subtableOrdinal++;
            List<Row> rows = subtable.rows();
            if (rows.isEmpty()) continue;

            int r = 0;
            if (activeRowTypes.size() == 1 && activeRowTypes.get(0).cardinality == 4) {
                PatternDef.RowTypeDef rt = activeRowTypes.get(0);
                int n = rt.exactCount;
                while (r < rows.size()) {
                    int blockStart = r;
                    for (int k = 0; k < n && r < rows.size(); k++) {
                        if (!rowMatchesRowType(syntax, rows.get(r).index(), rt)) {
                            break;
                        }
                        appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                        r++;
                    }
                    if (r == blockStart) {
                        break;
                    }
                    if (r - blockStart < n) {
                        break;
                    }
                }
                continue;
            }
            int ti = 0;
            while (ti < activeRowTypes.size() && r < rows.size()) {
                PatternDef.RowTypeDef rt = activeRowTypes.get(ti);
                if (rt.cardinality == 1) {
                    appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                    r++;
                    ti++;
                } else if (rt.cardinality == -1) {
                    boolean any = false;
                    while (r < rows.size()) {
                        if (!rowMatchesRowType(syntax, rows.get(r).index(), rt)) {
                            break;
                        }
                        appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                        r++;
                        any = true;
                    }
                    if (!any) {
                        break;
                    }
                    ti++;
                } else if (rt.cardinality == 2) {
                    if (r < rows.size() && rowMatchesRowType(syntax, rows.get(r).index(), rt)) {
                        appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                        r++;
                    }
                    ti++;
                } else if (rt.cardinality == 3) {
                    while (r < rows.size() && rowMatchesRowType(syntax, rows.get(r).index(), rt)) {
                        appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                        r++;
                    }
                    ti++;
                } else if (rt.cardinality == 4) {
                    int n = rt.exactCount;
                    for (int k = 0; k < n && r < rows.size(); k++) {
                        if (!rowMatchesRowType(syntax, rows.get(r).index(), rt)) {
                            break;
                        }
                        appendItemsFromRowType(syntax, rows.get(r).index(), rt, allCdi, contextItems, actions);
                        r++;
                    }
                    ti++;
                } else {
                    break;
                }
            }
        }

        TableSemantics semantics = new TableSemantics(allCdi, contextItems, actions);
        return new InterpretableTable(syntax, semantics);
    }

    private static int repeatedTailRowTypeStart(PatternDef patternDef, TableSyntax syntax) {
        if (patternDef.subtableCountKind() != PatternDef.SubtableCountKind.ONE || syntax.subtables().size() <= 1) {
            return -1;
        }
        List<PatternDef.RowTypeDef> rowTypes = patternDef.rowTypes();
        for (int i = 0; i < rowTypes.size(); i++) {
            PatternDef.RowTypeDef rt = rowTypes.get(i);
            if (rt.cardinality != 1 && rt.cardinality != 4) {
                return i > 0 ? i : -1;
            }
        }
        return -1;
    }

    private static ContextDerivedItem getOrCreateContextItem(
            Set<ContextDerivedItem> contextItems, ProviderSpec.ContextLiteralSpec spec) {
        for (ContextDerivedItem c : contextItems) {
            if (c.str().equals(spec.text()) && c.type() == spec.type()) {
                return c;
            }
        }
        ContextDerivedItem n = new ContextDerivedItem(spec.text(), spec.type());
        contextItems.add(n);
        return n;
    }

    private static ContextDerivedItem getOrCreateContextAttribute(Set<ContextDerivedItem> contextItems, String name) {
        return getOrCreateContextItem(contextItems, new ProviderSpec.ContextLiteralSpec(name, ItemType.ATTRIBUTE));
    }

    private static ItemProvider toItemProvider(
            ProviderSpec ps,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            boolean excludeAnchorFromCandidates) {
        if (ps.contextLiteral() != null) {
            ProviderSpec.ContextLiteralSpec lit = ps.contextLiteral();
            ContextDerivedItem item = getOrCreateContextItem(contextItems, lit);
            ContextDerivedProviderKind kind = switch (lit.type()) {
                case VALUE -> ContextDerivedProviderKind.VAL;
                case ATTRIBUTE -> ContextDerivedProviderKind.ATTR;
                case AUXILIARY -> ContextDerivedProviderKind.AUX;
            };
            return new ContextDerivedItemProvider(List.of(item), kind);
        }
        return new CellDerivedItemProvider(
                ps.predicate(),
                ps.traversal(),
                allCdi,
                ps.cardinality(),
                ps.cellKind(),
                excludeAnchorFromCandidates);
    }

    private static ItemType itemTypeFromGroup(CellGroupSpec spec) {
        if (spec.auxiliaryItem()) {
            return ItemType.AUXILIARY;
        }
        if (spec.attributeItem()) {
            return ItemType.ATTRIBUTE;
        }
        return ItemType.VALUE;
    }

    private static void appendItemsFromRowType(
            TableSyntax syntax,
            int rowIndex,
            PatternDef.RowTypeDef rt,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {
        if (rt.subrowsBlock == null) {
            appendItemsFromGroups(syntax, rowIndex, 0, rt.groups, allCdi, contextItems, actions);
            return;
        }
        PatternDef.SubrowsBlock sb = rt.subrowsBlock;
        int numCols = syntax.numCols();
        int col = appendItemsFromGroups(syntax, rowIndex, 0, rt.groups, allCdi, contextItems, actions);
        if (col > 0) {
            syntax.defineSubrow(rowIndex, 0, col - 1);
        }
        int w = fixedInnerWidth(sb.innerGroups);
        int rem = numCols - col;
        if (w <= 0 || rem % w != 0) {
            throw new IllegalStateException(
                    "subrows: remainder columns " + rem + " not divisible by inner width " + w);
        }
        int n = rem / w;
        for (int rep = 0; rep < n; rep++) {
            syntax.defineSubrow(rowIndex, col, col + w - 1);
            col = appendItemsFromGroups(syntax, rowIndex, col, sb.innerGroups, allCdi, contextItems, actions);
        }
        if (col != numCols) {
            throw new IllegalStateException("subrows: expected to consume all columns, col=" + col + " numCols=" + numCols);
        }
    }

    private static int fixedInnerWidth(List<CellGroupSpec> innerGroups) {
        int w = 0;
        for (CellGroupSpec g : innerGroups) {
            if (g.cellCount() == -1) {
                throw new IllegalStateException("subrows inner groups must have fixed width");
            }
            w += g.cellCount();
        }
        return w;
    }

    /**
     * Emits cell-derived items for a contiguous list of cell groups starting at {@code startCol};
     * returns the next column index after the matched groups.
     */
    private static int appendItemsFromGroups(
            TableSyntax syntax,
            int rowIndex,
            int startCol,
            List<CellGroupSpec> groups,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {
        int col = startCol;
        int numCols = syntax.numCols();
        for (CellGroupSpec spec : groups) {
            int consume = spec.cellCount() == -1 ? (numCols - col) : spec.cellCount();
            if (consume <= 0) break;
            if (spec.compound() != null) {
                if (col >= numCols) break;
                appendCompoundItems(syntax, rowIndex, col, spec, allCdi, contextItems, actions);
                col += 1;
                continue;
            }
            if (spec.delimited() != null) {
                if (col >= numCols) break;
                appendDelimitedItems(syntax, rowIndex, col, spec, allCdi, contextItems, actions);
                col += 1;
                continue;
            }
            if (!spec.isSkip()) {
                for (int i = 0; i < consume && col < numCols; i++, col++) {
                    Cell cell = syntax.getCell(rowIndex, col);
                    if (spec.branchWhen() != null && !spec.alwaysEmit()
                            && spec.skipWhenBranchMatches() == spec.branchWhen().test(cell)) {
                        continue;
                    }
                    List<String> tags = spec.itemTags();
                    String raw = cell.text() == null ? "" : cell.text();
                    if (spec.valueTextTransform() != null) {
                        raw = spec.valueTextTransform().apply(cell);
                        if (raw == null) {
                            raw = "";
                        }
                    }
                    ItemType itemType = itemTypeFromGroup(spec);
                    CellDerivedItem item = tags.isEmpty()
                            ? new CellDerivedItem(raw, 0, cell, itemType)
                            : new CellDerivedItem(raw, tags, 0, cell, itemType);
                    allCdi.add(item);

                    if (spec.avpLiteralAttribute() != null) {
                        ContextDerivedItem ctxAttr = getOrCreateContextAttribute(
                                contextItems, spec.avpLiteralAttribute());
                        actions.add(new InterpretationAction(
                                item,
                                List.of(new ContextDerivedItemProvider(
                                        List.of(ctxAttr), ContextDerivedProviderKind.ATTR)),
                                new AvpOperation()));
                    }
                    if (spec.isAnchor() && spec.hasRec()) {
                        List<ItemProvider> providers = new ArrayList<>();
                        for (ProviderSpec rps : spec.recProviders()) {
                            providers.add(toItemProvider(rps, allCdi, contextItems, true));
                        }
                        actions.add(new InterpretationAction(item, providers, new RecOperation()));
                    }
                    if (spec.concatPredicate() != null) {
                        actions.add(new InterpretationAction(
                                item,
                                List.of(new CellDerivedItemProvider(
                                        spec.concatPredicate(), TraversalOrder.ROW_MAJOR, allCdi)),
                                new ConcatOperation()));
                    }
                    if (spec.avpPredicate() != null) {
                        actions.add(new InterpretationAction(
                                item,
                                List.of(new CellDerivedItemProvider(
                                        spec.avpPredicate(), TraversalOrder.ROW_MAJOR, allCdi, 1,
                                        CellDerivedProviderKind.ATTR)),
                                new AvpOperation()));
                    }
                    if (spec.fillSpec() != null || spec.prefixSpec() != null || spec.suffixSpec() != null) {
                        boolean doJoin = spec.branchWhen() == null
                                || spec.alwaysEmit() && spec.branchWhen().test(cell);
                        if (doJoin) {
                            if (spec.fillSpec() != null) {
                                FillSpec fs = spec.fillSpec();
                                actions.add(new InterpretationAction(
                                        item, itemProvidersForJoin(fs.providers(), allCdi, contextItems, false),
                                        new FillOperation(fs.delimiter())));
                            }
                            if (spec.prefixSpec() != null) {
                                PrefixSpec ps = spec.prefixSpec();
                                actions.add(new InterpretationAction(
                                        item, itemProvidersForJoin(ps.providers(), allCdi, contextItems, true),
                                        new PrefixOperation(ps.delimiter())));
                            }
                            if (spec.suffixSpec() != null) {
                                SuffixSpec ss = spec.suffixSpec();
                                actions.add(new InterpretationAction(
                                        item, itemProvidersForJoin(ss.providers(), allCdi, contextItems, true),
                                        new SuffixOperation(ss.delimiter())));
                            }
                        }
                    }
                }
            } else {
                col += consume;
            }
        }
        return col;
    }

    private static List<ItemProvider> itemProvidersForJoin(
            List<ProviderSpec> specs,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            boolean excludeAnchorFromCandidates) {
        List<ItemProvider> list = new ArrayList<>(specs.size());
        for (ProviderSpec ps : specs) {
            list.add(toItemProvider(ps, allCdi, contextItems, excludeAnchorFromCandidates));
        }
        return list;
    }

    /**
     * One segment per compound token: each separator is matched in order against the remainder of the text
     * (first {@link String#indexOf(String)}). Returns null if a separator is missing.
     */
    private static String[] splitCompoundParts(String text, CompoundSplitSpec cs) {
        var seps = cs.separators();
        var toks = cs.tokens();
        int n = toks.size();
        String rest = text;
        String[] parts = new String[n];
        for (int i = 0; i < seps.size(); i++) {
            String sep = seps.get(i);
            int pos = rest.indexOf(sep);
            if (pos < 0) {
                return null;
            }
            parts[i] = rest.substring(0, pos);
            rest = rest.substring(pos + sep.length());
        }
        parts[n - 1] = rest;
        return parts;
    }

    private static void appendCompoundItems(
            TableSyntax syntax,
            int rowIndex,
            int col,
            CellGroupSpec spec,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {
        CompoundSplitSpec cs = spec.compound();
        Cell cell = syntax.getCell(rowIndex, col);
        if (spec.cellPredicate() != null && !spec.cellPredicate().test(cell)) {
            return;
        }
        if (spec.branchWhen() != null && !spec.alwaysEmit()
                && spec.skipWhenBranchMatches() == spec.branchWhen().test(cell)) {
            return;
        }
        String text = cell.text() == null ? "" : cell.text();
        String[] parts = splitCompoundParts(text, cs);
        if (parts == null) {
            return;
        }
        List<String> tags = spec.itemTags();
        for (int i = 0; i < parts.length; i++) {
            CompoundTokenSpec ts = cs.tokens().get(i);
            if (!ts.emit()) {
                continue;
            }
            String tok = parts[i];
            ItemType itemType = ts.attributeItem() ? ItemType.ATTRIBUTE : ItemType.VALUE;
            CellDerivedItem item = tags.isEmpty()
                    ? new CellDerivedItem(tok, i, cell, itemType)
                    : new CellDerivedItem(tok, tags, i, cell, itemType);
            allCdi.add(item);
            if (spec.avpPredicate() != null && itemType == ItemType.VALUE) {
                actions.add(new InterpretationAction(
                        item,
                        List.of(new CellDerivedItemProvider(
                                spec.avpPredicate(), TraversalOrder.ROW_MAJOR, allCdi, 1,
                                CellDerivedProviderKind.ATTR)),
                        new AvpOperation()));
            }
            if (ts.recAnchor() && !ts.recProviders().isEmpty()) {
                List<ItemProvider> providers = new ArrayList<>();
                for (ProviderSpec rps : ts.recProviders()) {
                    providers.add(toItemProvider(rps, allCdi, contextItems, true));
                }
                actions.add(new InterpretationAction(item, providers, new RecOperation()));
            }
        }
    }

    private static void appendDelimitedItems(
            TableSyntax syntax,
            int rowIndex,
            int col,
            CellGroupSpec spec,
            Set<CellDerivedItem> allCdi,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {
        DelimitedSplitSpec ds = spec.delimited();
        Cell cell = syntax.getCell(rowIndex, col);
        if (spec.cellPredicate() != null && !spec.cellPredicate().test(cell)) {
            return;
        }
        if (spec.branchWhen() != null && !spec.alwaysEmit()
                && spec.skipWhenBranchMatches() == spec.branchWhen().test(cell)) {
            return;
        }
        String text = cell.text() == null ? "" : cell.text();
        String[] raw = text.split(Pattern.quote(ds.delimiter()), -1);
        List<String> tags = spec.itemTags();
        ItemType delimitedItemType = itemTypeFromGroup(spec);
        int pos = 0;
        for (String part : raw) {
            String tok = part.trim();
            if (tok.isEmpty()) {
                continue;
            }
            CellDerivedItem item = tags.isEmpty()
                    ? new CellDerivedItem(tok, pos, cell, delimitedItemType)
                    : new CellDerivedItem(tok, tags, pos, cell, delimitedItemType);
            allCdi.add(item);
            pos++;

            if (spec.avpLiteralAttribute() != null) {
                ContextDerivedItem ctxAttr = getOrCreateContextAttribute(
                        contextItems, spec.avpLiteralAttribute());
                actions.add(new InterpretationAction(
                        item,
                        List.of(new ContextDerivedItemProvider(
                                List.of(ctxAttr), ContextDerivedProviderKind.ATTR)),
                        new AvpOperation()));
            }
            if (spec.recProviders() != null && !spec.recProviders().isEmpty()) {
                List<ItemProvider> providers = new ArrayList<>();
                for (ProviderSpec rps : spec.recProviders()) {
                    providers.add(toItemProvider(rps, allCdi, contextItems, true));
                }
                actions.add(new InterpretationAction(item, providers, new RecOperation()));
            }
            if (spec.concatPredicate() != null) {
                actions.add(new InterpretationAction(
                        item,
                        List.of(new CellDerivedItemProvider(
                                spec.concatPredicate(), TraversalOrder.ROW_MAJOR, allCdi)),
                        new ConcatOperation()));
            }
            if (spec.avpPredicate() != null) {
                actions.add(new InterpretationAction(
                        item,
                        List.of(new CellDerivedItemProvider(
                                spec.avpPredicate(), TraversalOrder.ROW_MAJOR, allCdi, 1,
                                CellDerivedProviderKind.ATTR)),
                        new AvpOperation()));
            }
        }
    }

    /**
     * Infers subtable boundaries from the pattern and row content, then calls defineSubtables.
     * If {@link PatternDef#subtableCountKind()} is {@link PatternDef.SubtableCountKind#ONE}, only {@code defineSubtables(0)}
     * is applied (whole sheet).
     * If {@link PatternDef#effectiveRowsPerSubtable()} is positive (sum of fixed row types: each {@code rows().one()}
     * counts as 1 row, each {@code rows().exactly(k)} as {@code k}), boundaries are every N rows.
     * Else if the pattern has a trailing {@code zeroOrOne} row type with only skip groups, a boundary
     * is placed after each row that matches that separator type and whose following row
     * matches the first row type (index 0) or the second (index 1), when present.
     * The second case covers blocks that begin with a summary row only (no template rows).
     * This avoids treating the internal blank row (e.g. after a title) as an inter-block separator when it
     * is also {@code "","":} like the true separator.
     * Otherwise rows matching the first row type (index 0) start a new subtable.
     * <p>
     * For boundary detection only, {@code skip()} without a cell predicate is treated as matching
     * blank cells (see {@link #rowMatchesRowType(TableSyntax, int, PatternDef.RowTypeDef, boolean)}).
     * That avoids false “start of block” when the first row type and data rows share the same width
     * but differ by padding; application-time matching still treats plain {@code skip()} as any content.
     */
    private static void inferAndApplySubtables(PatternDef patternDef, TableSyntax syntax) {
        List<PatternDef.RowTypeDef> rowTypes = patternDef.rowTypes();
        if (rowTypes.isEmpty()) {
            return;
        }

        int numRows = syntax.numRows();
        PatternDef.SubtableCountKind subKind = patternDef.subtableCountKind();

        if (subKind == PatternDef.SubtableCountKind.ONE) {
            if (numRows > 0) {
                int[] boundaries = inferSingleSheetBlockBoundaries(patternDef, syntax);
                syntax.defineSubtables(boundaries);
            }
            return;
        }

        int fixed = patternDef.effectiveRowsPerSubtable();
        if (fixed > 0 && allRowTypesFixed(rowTypes)) {
            if (numRows == 0) {
                return;
            }
            List<Integer> boundaries = new ArrayList<>();
            for (int r = 0; r < numRows; r += fixed) {
                boundaries.add(r);
            }
            int[] arr = boundaries.stream().mapToInt(Integer::intValue).toArray();
            syntax.defineSubtables(arr);
            validateExactSubtableCount(patternDef, arr.length);
            return;
        }

        if (rowTypes.size() < 2) {
            validateExactSubtableCount(patternDef, 1);
            return;
        }

        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(0);

        Integer sepIdx = findSeparatorRowTypeIndex(rowTypes);
        if (sepIdx != null) {
            PatternDef.RowTypeDef sepType = rowTypes.get(sepIdx);
            PatternDef.RowTypeDef headType = rowTypes.get(0);
            PatternDef.RowTypeDef headAfterSepAlt =
                    rowTypes.size() > 1 ? rowTypes.get(1) : null;
            for (int r = 0; r < numRows; r++) {
                if (!rowMatchesRowType(syntax, r, sepType, true) || r + 1 >= numRows) {
                    continue;
                }
                int next = r + 1;
                boolean startsBlock = rowMatchesRowType(syntax, next, headType, true)
                        || (headAfterSepAlt != null && rowMatchesRowType(syntax, next, headAfterSepAlt, true));
                if (startsBlock) {
                    boundaries.add(r + 1);
                }
            }
        } else {
            int numCols = syntax.numCols();
            int[] valueColEnd = computeValueColEndPerType(rowTypes, numCols);
            boolean useRowType0Matcher =
                    discriminatingRegionsEmpty(numCols, valueColEnd, rowTypes.size());
            PatternDef.RowTypeDef blockStartMatcher = rowTypes.get(0);
            if (useRowType0Matcher && isHeaderOnlyRowType(rowTypes.get(0)) && rowTypes.size() > 1) {
                blockStartMatcher = rowTypes.get(1);
            }
            for (int r = 0; r < numRows; r++) {
                if (r == 0) continue;
                boolean isStartOfBlock;
                if (useRowType0Matcher) {
                    isStartOfBlock = rowMatchesRowType(syntax, r, blockStartMatcher, true);
                } else {
                    int matchedType = matchRowToType(syntax, r, numCols, rowTypes, valueColEnd);
                    isStartOfBlock = matchedType == 0;
                }
                if (isStartOfBlock) {
                    boundaries.add(r);
                }
            }
        }

        int[] arr = boundaries.stream().mapToInt(Integer::intValue).toArray();
        syntax.defineSubtables(arr);
        validateExactSubtableCount(patternDef, arr.length);
    }

    /**
     * When the pattern declares {@code subtables().exactly(n)}, the number of boundary starts (subtables) must match.
     */
    private static void validateExactSubtableCount(PatternDef patternDef, int subtableCount) {
        if (patternDef.subtableCountKind() != PatternDef.SubtableCountKind.EXACTLY) {
            return;
        }
        int expected = patternDef.exactSubtableCount();
        if (subtableCount != expected) {
            throw new IllegalStateException(
                    "Pattern declares subtables().exactly(" + expected + ") but partitioning yields " + subtableCount
                            + " subtable(s)");
        }
    }

    private static boolean isRowBlank(TableSyntax syntax, int row) {
        for (int c = 0; c < syntax.numCols(); c++) {
            String t = syntax.getCell(row, c).text();
            if (t != null && !t.isBlank()) return false;
        }
        return true;
    }

    private static boolean allRowTypesFixed(List<PatternDef.RowTypeDef> rowTypes) {
        for (PatternDef.RowTypeDef rt : rowTypes) {
            if (rt.cardinality != 1 && rt.cardinality != 4) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHeaderOnlyRowType(PatternDef.RowTypeDef rt) {
        if (rt.groups.isEmpty()) return false;
        boolean allSkip = rt.groups.stream().allMatch(CellGroupSpec::isSkip);
        if (!allSkip) return false;
        boolean anyPredicate = rt.groups.stream().anyMatch(g -> g.cellPredicate() != null);
        return !anyPredicate;
    }

    /**
     * For {@code subtables().one()}, keep the old whole-sheet behaviour by default, but allow
     * a fixed leading header block followed by repeated blank-separated data blocks.
     * This matches patterns like Task48 where the first rows are global header and each
     * subsequent blank-separated block should form its own subtable.
     */
    private static int[] inferSingleSheetBlockBoundaries(PatternDef patternDef, TableSyntax syntax) {
        List<PatternDef.RowTypeDef> rowTypes = patternDef.rowTypes();
        int prefixRows = 0;
        int variableIdx = -1;
        for (int i = 0; i < rowTypes.size(); i++) {
            PatternDef.RowTypeDef rt = rowTypes.get(i);
            if (rt.cardinality == 1) {
                prefixRows += 1;
                continue;
            }
            if (rt.cardinality == 4) {
                prefixRows += rt.exactCount;
                continue;
            }
            variableIdx = i;
            break;
        }
        if (variableIdx < 0 || prefixRows <= 0 || prefixRows >= syntax.numRows()) {
            return new int[]{0};
        }

        PatternDef.RowTypeDef variable = rowTypes.get(variableIdx);
        if (variable.cardinality != -1) {
            return new int[]{0};
        }

        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(0);
        boundaries.add(prefixRows);
        boolean foundRepeatedBlock = false;
        for (int r = prefixRows; r < syntax.numRows() - 1; r++) {
            if (!isRowBlank(syntax, r)) {
                continue;
            }
            if (!rowMatchesRowType(syntax, r, variable, false)) {
                continue;
            }
            int next = r + 1;
            if (!rowMatchesRowType(syntax, next, variable, false)) {
                continue;
            }
            boundaries.add(next);
            foundRepeatedBlock = true;
        }
        if (!foundRepeatedBlock) {
            return new int[]{0};
        }
        return boundaries.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Last {@code zeroOrOne} row type whose groups are all skip — used as subtable separator row.
     */
    private static Integer findSeparatorRowTypeIndex(List<PatternDef.RowTypeDef> rowTypes) {
        for (int i = rowTypes.size() - 1; i >= 0; i--) {
            PatternDef.RowTypeDef rt = rowTypes.get(i);
            if (rt.cardinality != 2) continue;
            if (rt.groups.isEmpty()) continue;
            boolean allSkip = rt.groups.stream().allMatch(CellGroupSpec::isSkip);
            if (allSkip) {
                return i;
            }
        }
        return null;
    }

    /**
     * Whether {@code row} matches the row-type pattern (cell groups and optional cell predicates).
     *
     * @param plainSkipRequiresBlank if {@code true} (subtable inference only), each cell in a {@code skip()}
     *                               group without an explicit {@code check(...)} must be blank; if {@code false}
     *                               (application), such cells match any content.
     */
    private static boolean rowMatchesRowType(
            TableSyntax syntax, int row, PatternDef.RowTypeDef rowType, boolean plainSkipRequiresBlank) {
        int numCols = syntax.numCols();
        boolean allSkip = !rowType.groups.isEmpty() && rowType.groups.stream().allMatch(CellGroupSpec::isSkip);
        boolean anyCellPredicate = rowType.groups.stream()
                .anyMatch(g -> g.cellPredicate() != null);
        if (allSkip && !anyCellPredicate && plainSkipRequiresBlank) {
            return isRowBlank(syntax, row);
        }
        int col = tryMatchGroupsAt(syntax, row, rowType.groups, 0, numCols, plainSkipRequiresBlank);
        if (col < 0) {
            return false;
        }
        if (rowType.subrowsBlock == null) {
            return col == numCols;
        }
        PatternDef.SubrowsBlock sb = rowType.subrowsBlock;
        int w = fixedInnerWidth(sb.innerGroups);
        int rem = numCols - col;
        if (w <= 0 || rem % w != 0) {
            return false;
        }
        int n = rem / w;
        if (sb.cardinality == 1 && n != 1) {
            return false;
        }
        if (sb.cardinality == 4 && n != sb.exactCount) {
            return false;
        }
        if (sb.cardinality == -1 && n < 1) {
            return false;
        }
        int c = col;
        for (int rep = 0; rep < n; rep++) {
            int end = tryMatchGroupsAt(syntax, row, sb.innerGroups, c, numCols, plainSkipRequiresBlank);
            if (end < 0 || end != c + w) {
                return false;
            }
            c = end;
        }
        return c == numCols;
    }

    /**
     * Tries to match {@code groups} starting at {@code startCol}; returns next column index, or {@code -1} on failure.
     */
    private static int tryMatchGroupsAt(
            TableSyntax syntax,
            int row,
            List<CellGroupSpec> groups,
            int startCol,
            int numCols,
            boolean plainSkipRequiresBlank) {
        int col = startCol;
        for (CellGroupSpec spec : groups) {
            int consume = spec.cellCount() == -1 ? (numCols - col) : spec.cellCount();
            if (consume <= 0) {
                return -1;
            }
            if (spec.compound() != null) {
                if (col >= numCols) {
                    return -1;
                }
                CompoundSplitSpec cs = spec.compound();
                Cell cell = syntax.getCell(row, col);
                Predicate<Cell> p = spec.cellPredicate();
                if (p != null && !p.test(cell)) {
                    return -1;
                }
                if (spec.branchWhen() != null && !spec.alwaysEmit()
                        && spec.skipWhenBranchMatches() == spec.branchWhen().test(cell)) {
                    col += 1;
                    continue;
                }
                String text = cell.text() == null ? "" : cell.text();
                String[] parts = splitCompoundParts(text, cs);
                if (parts == null) {
                    return -1;
                }
                col += 1;
                continue;
            }
            if (spec.delimited() != null) {
                if (col >= numCols) {
                    return -1;
                }
                Cell cell = syntax.getCell(row, col);
                Predicate<Cell> p = spec.cellPredicate();
                if (p != null && !p.test(cell)) {
                    return -1;
                }
                if (spec.branchWhen() != null && !spec.alwaysEmit()
                        && spec.skipWhenBranchMatches() == spec.branchWhen().test(cell)) {
                    col += 1;
                    continue;
                }
                col += 1;
                continue;
            }
            for (int i = 0; i < consume && col < numCols; i++, col++) {
                Cell cell = syntax.getCell(row, col);
                Predicate<Cell> p = spec.cellPredicate();
                if (p != null) {
                    if (!p.test(cell)) {
                        return -1;
                    }
                } else if (spec.isSkip()) {
                    if (plainSkipRequiresBlank) {
                        String t = cell.text();
                        if (t != null && !t.isBlank()) {
                            return -1;
                        }
                    }
                }
            }
        }
        return col;
    }

    /** Application-time matching: plain {@code skip()} does not require blank cells. */
    private static boolean rowMatchesRowType(TableSyntax syntax, int row, PatternDef.RowTypeDef rowType) {
        return rowMatchesRowType(syntax, row, rowType, false);
    }

    private static int[] computeValueColEndPerType(List<PatternDef.RowTypeDef> rowTypes, int numCols) {
        int[] result = new int[rowTypes.size()];
        for (int t = 0; t < rowTypes.size(); t++) {
            PatternDef.RowTypeDef rt = rowTypes.get(t);
            if (rt.subrowsBlock != null) {
                result[t] = numCols - 1;
                continue;
            }
            int end = -1;
            for (CellGroupSpec spec : rt.groups) {
                if (spec.cellCount() == -1) {
                    if (!spec.isSkip()) {
                        end = numCols - 1;
                    }
                    break;
                }
                end += spec.cellCount();
            }
            result[t] = end;
        }
        return result;
    }

    /**
     * When every pair of consecutive row types uses the full row width, there is no column slice left
     * to discriminate types in {@link #matchRowToType}; fall back to matching the first row type (e.g. L1).
     */
    private static boolean discriminatingRegionsEmpty(int numCols, int[] valueColEnd, int numRowTypes) {
        for (int i = 1; i < numRowTypes; i++) {
            int start = valueColEnd[i - 1] + 1;
            int end = Math.min(valueColEnd[i], numCols - 1);
            if (start <= end) {
                return false;
            }
        }
        return numRowTypes >= 2;
    }

    /**
     * Matches row r to a row type. Type i has values in cols 0..valueColEnd[i].
     * The discriminating region between type i and i+1 is cols (valueColEnd[i]+1)..valueColEnd[i+1].
     * If row has content there, it matches type i+1 or higher.
     */
    private static int matchRowToType(TableSyntax syntax, int row, int numCols,
                                       List<PatternDef.RowTypeDef> rowTypes, int[] valueColEnd) {
        boolean anyNonBlank = false;
        for (int c = 0; c < numCols; c++) {
            String t = syntax.getCell(row, c).text();
            if (t != null && !t.isBlank()) {
                anyNonBlank = true;
                break;
            }
        }
        // Blank spacer row between header and data must not match type 0, or subtable inference
        // inserts a false boundary and restarts row typing inside the next fragment.
        if (!anyNonBlank && rowTypes.size() >= 3) {
            return 1;
        }
        int matched = 0;
        for (int i = 1; i < rowTypes.size(); i++) {
            int start = valueColEnd[i - 1] + 1;
            int end = Math.min(valueColEnd[i], numCols - 1);
            boolean hasContent = false;
            for (int c = start; c <= end; c++) {
                String text = syntax.getCell(row, c).text();
                if (text != null && !text.isBlank()) {
                    hasContent = true;
                    break;
                }
            }
            if (hasContent) matched = i;
        }
        return matched;
    }
}
