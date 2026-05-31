package ru.icc.regtab.atp.match;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.*;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Semantic layer construction (Section 6.2): traverses matched pairs M
 * to derive items and construct interpretation actions, building the
 * semantic layer of an InterpretableTable.
 */
public final class SemanticConstructor {

    private SemanticConstructor() {
    }

    public static InterpretableTable construct(
            TableSyntax syntax,
            List<MatchedPair> matchedPairs,
            Set<ContextDerivedItem> contextItems) {

        var cellDerivedItems = new LinkedHashSet<CellDerivedItem>();
        var contextItemSet = new LinkedHashSet<>(contextItems);
        var actions = new ArrayList<InterpretationAction>();

        for (MatchedPair pair : matchedPairs) {
            processMatchedPair(pair, cellDerivedItems, contextItemSet, actions);
        }

        var semantics = new TableSemantics(cellDerivedItems, contextItemSet, actions);
        return new InterpretableTable(syntax, semantics);
    }

    public static InterpretableTable construct(
            TableSyntax syntax, List<MatchedPair> matchedPairs) {
        return construct(syntax, matchedPairs, Set.of());
    }

    private static void processMatchedPair(
            MatchedPair pair,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {

        CellPattern pattern = pair.pattern();
        Cell cell = pair.cell();
        ContentSpec cs = pattern.contentSpec();
        if (cs == null) {
            return;
        }

        processContentSpec(cs, cell, allItems, contextItems, actions);
    }

    private static void processContentSpec(
            ContentSpec cs,
            Cell cell,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {
        switch (cs) {
            case AtomicContentSpec a -> processAtomic(a, cell, cell.text(), 0, allItems, contextItems, actions);
            case DelimitedContentSpec d -> processDelimited(d, cell, allItems, contextItems, actions);
            case CompoundContentSpec comp -> processCompound(comp, cell, allItems, contextItems, actions);
            case ConditionalContentSpec cond -> {
                ContentSpec branch = cond.condition().test(cell) ? cond.positive() : cond.negative();
                processContentSpec(branch, cell, allItems, contextItems, actions);
            }
        }
    }

    private static void processAtomic(
            AtomicContentSpec atomicSpec,
            Cell cell,
            String inputText,
            int itemIndex,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {

        if (atomicSpec.idd() == ItemDerivationDirective.SKIP) {
            return;
        }

        String str = inputText;
        if (atomicSpec.extractor() != null) {
            str = atomicSpec.extractor().apply(inputText);
        }

        ItemType type = atomicSpec.idd().toItemType();
        var item = new CellDerivedItem(str, atomicSpec.tags(), itemIndex, cell, type);
        allItems.add(item);

        for (ActionSpec as : atomicSpec.actions()) {
            var action = instantiateAction(item, as, allItems, contextItems);
            actions.add(action);
        }
    }

    private static void processDelimited(
            DelimitedContentSpec delimSpec,
            Cell cell,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {

        String[] parts = cell.text().split(java.util.regex.Pattern.quote(delimSpec.delimiter()), -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                processAtomic(delimSpec.atomicSpec(), cell, part, i, allItems, contextItems, actions);
            }
        }
    }

    private static void processCompound(
            CompoundContentSpec compSpec,
            Cell cell,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            List<InterpretationAction> actions) {

        String text = cell.text();
        int pos = 0;
        int itemIndex = 0;
        List<CompoundSegment> segments = compSpec.segments();

        for (int i = 0; i < segments.size(); i++) {
            CompoundSegment seg = segments.get(i);
            if (!seg.leadingDelimiter().isEmpty()) {
                int delimIdx = text.indexOf(seg.leadingDelimiter(), pos);
                if (delimIdx < 0) {
                    throw new MatchException("Expected delimiter '" + seg.leadingDelimiter()
                            + "' not found in cell text at pos " + pos + ": '" + text + "'");
                }
                pos = delimIdx + seg.leadingDelimiter().length();
            }

            String nextDelim = i < segments.size() - 1
                    ? segments.get(i + 1).leadingDelimiter()
                    : compSpec.trailingDelimiter();

            int endPos;
            if (nextDelim != null && !nextDelim.isEmpty()) {
                endPos = text.indexOf(nextDelim, pos);
                if (endPos < 0) {
                    endPos = text.length();
                }
            } else {
                endPos = text.length();
            }

            String substring = text.substring(pos, endPos);
            ContentSpec segSpec = seg.spec();
            if (segSpec instanceof AtomicContentSpec a) {
                processAtomic(a, cell, substring, itemIndex, allItems, contextItems, actions);
                itemIndex++;
            } else if (segSpec instanceof DelimitedContentSpec d) {
                String[] parts = substring.split(java.util.regex.Pattern.quote(d.delimiter()), -1);
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        processAtomic(d.atomicSpec(), cell, trimmed, itemIndex, allItems, contextItems, actions);
                        itemIndex++;
                    }
                }
            }
            if (nextDelim != null && !nextDelim.isEmpty()) {
                pos = endPos;
            }
        }
    }

    private static InterpretationAction instantiateAction(
            CellDerivedItem anchor,
            ActionSpec actionSpec,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems) {

        WorkingStateOperation operation = createOperation(actionSpec);
        List<ItemProvider> providers = new ArrayList<>();
        for (ProviderSpec ps : actionSpec.providers()) {
            providers.add(toItemProvider(ps, allItems, contextItems, actionSpec.inherited()));
        }
        return new InterpretationAction(anchor, providers, operation);
    }

    private static ItemProvider toItemProvider(
            ProviderSpec spec,
            Set<CellDerivedItem> allItems,
            Set<ContextDerivedItem> contextItems,
            boolean lenient) {
        if (spec.isContextLiteral()) {
            if (spec.contextLiteral().constValue() != null) {
                ContextDerivedItem item = new ContextDerivedItem(
                        spec.contextLiteral().text(), ItemType.ATTRIBUTE,
                        spec.contextLiteral().constValue());
                return new ContextDerivedItemProvider(List.of(item), ContextDerivedProviderKind.UNRESTRICTED);
            }
            ContextDerivedItem item = getOrCreateContextItem(contextItems, spec.contextLiteral());
            return new ContextDerivedItemProvider(List.of(item), spec.contextLiteral().kind());
        }
        return new CellDerivedItemProvider(
                spec.filterCondition().toCondition(),
                spec.traversalOrder(),
                allItems,
                spec.cardinality(),
                spec.targetItemKind(),
                true,     // excludeAnchorFromCandidates (same as 5-arg default)
                lenient);
    }

    private static ContextDerivedItem getOrCreateContextItem(
            Set<ContextDerivedItem> contextItems,
            ProviderSpec.ContextLiteralSpec spec) {
        for (ContextDerivedItem item : contextItems) {
            if (item.str().equals(spec.text()) && item.type() == spec.type()) {
                return item;
            }
        }
        ContextDerivedItem created = new ContextDerivedItem(spec.text(), spec.type());
        contextItems.add(created);
        return created;
    }

    private static WorkingStateOperation createOperation(ActionSpec as) {
        String delim = as.delimiter() != null ? as.delimiter() : "";
        return switch (as.operationType()) {
            case FILL -> new FillOperation(delim);
            case PREFIX -> new PrefixOperation(delim);
            case SUFFIX -> new SuffixOperation(delim);
            case AVP -> new AvpOperation();
            case REC -> new RecOperation();
            case JOIN -> new JoinOperation(as.keyPositions());
        };
    }

    public static final class MatchException extends RuntimeException {
        public MatchException(String message) {
            super(message);
        }
    }
}
