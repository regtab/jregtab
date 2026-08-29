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
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        var ctx = new Context(cellDerivedItems, contextItemSet);
        var actions = new ArrayList<InterpretationAction>();

        for (MatchedPair pair : matchedPairs) {
            processMatchedPair(pair, ctx, actions);
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
            Context ctx,
            List<InterpretationAction> actions) {

        CellPattern pattern = pair.pattern();
        Cell cell = pair.cell();
        ContentSpec cs = pattern.contentSpec();
        if (cs == null) {
            return;
        }

        processContentSpec(cs, cell, ctx, actions);
    }

    private static void processContentSpec(
            ContentSpec cs,
            Cell cell,
            Context ctx,
            List<InterpretationAction> actions) {
        switch (cs) {
            case AtomicContentSpec a -> processAtomic(a, cell, cell.text(), 0, ctx, actions);
            case DelimitedContentSpec d -> processDelimited(d, cell, ctx, actions);
            case CompoundContentSpec comp -> processCompound(comp, cell, ctx, actions);
            case ConditionalContentSpec cond -> {
                ContentSpec branch = cond.condition().test(cell) ? cond.positive() : cond.negative();
                processContentSpec(branch, cell, ctx, actions);
            }
        }
    }

    private static void processAtomic(
            AtomicContentSpec atomicSpec,
            Cell cell,
            String inputText,
            int itemIndex,
            Context ctx,
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
        ctx.index.items().add(item);

        for (ActionSpec as : atomicSpec.actions()) {
            actions.add(ctx.instantiateAction(item, as));
        }
    }

    /**
     * Delimited content (def:delimited-content-spec): the input text decomposes as
     * s₁ · δ · s₂ · δ ⋯ δ · sₙ with sₖ ∈ Σ*, and S_atom is applied to each sₖ
     * <em>verbatim</em>: substrings are never trimmed and empty substrings are never
     * dropped, so n items are always derived from n substrings.
     * <p>
     * Whitespace removal is opt-in through the atom's string extractor ξ
     * ({@code =TRIM}, {@code =NORM}), applied per token by {@link #processAtomic}.
     */
    private static void processDelimited(
            DelimitedContentSpec delimSpec,
            Cell cell,
            Context ctx,
            List<InterpretationAction> actions) {

        String[] parts = cell.text().split(java.util.regex.Pattern.quote(delimSpec.delimiter()), -1);
        for (int i = 0; i < parts.length; i++) {
            processAtomic(delimSpec.atomicSpec(), cell, parts[i], i, ctx, actions);
        }
    }

    private static void processCompound(
            CompoundContentSpec compSpec,
            Cell cell,
            Context ctx,
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
                processAtomic(a, cell, substring, itemIndex, ctx, actions);
                itemIndex++;
            } else if (segSpec instanceof DelimitedContentSpec d) {
                // Same verbatim semantics as processDelimited: no trimming, empties kept.
                String[] parts = substring.split(java.util.regex.Pattern.quote(d.delimiter()), -1);
                for (String part : parts) {
                    processAtomic(d.atomicSpec(), cell, part, itemIndex, ctx, actions);
                    itemIndex++;
                }
            }
            if (nextDelim != null && !nextDelim.isEmpty()) {
                pos = endPos;
            }
        }
    }

    /**
     * Per-table construction context: the target set J with its shared spatial index, the context
     * items, and caches of providers and operations. A {@link ProviderSpec} / {@link ActionSpec} is
     * shared by every cell matched by the same cell pattern, and the runtime objects built from it
     * are immutable and stateless (the index is shared), so one instance serves all those actions —
     * this keeps the memory of the semantic layer linear in the number of cells with a small constant.
     */
    private static final class Context {
        final CellDerivedItemIndex index;
        final Set<ContextDerivedItem> contextItems;
        private final Map<ProviderSpec, ItemProvider> strictProviders = new IdentityHashMap<>();
        private final Map<ProviderSpec, ItemProvider> lenientProviders = new IdentityHashMap<>();
        private final Map<ActionSpec, WorkingStateOperation> operations = new IdentityHashMap<>();

        Context(Set<CellDerivedItem> cellDerivedItems, Set<ContextDerivedItem> contextItems) {
            // One spatial index over J, shared by every cell-derived provider of this table.
            this.index = new CellDerivedItemIndex(cellDerivedItems);
            this.contextItems = contextItems;
        }

        InterpretationAction instantiateAction(CellDerivedItem anchor, ActionSpec actionSpec) {
            WorkingStateOperation operation = operations.computeIfAbsent(actionSpec, Context::createOperation);
            Map<ProviderSpec, ItemProvider> cache = actionSpec.inherited() ? lenientProviders : strictProviders;
            List<ItemProvider> providers = new ArrayList<>(actionSpec.providers().size());
            for (ProviderSpec ps : actionSpec.providers()) {
                if (ps.isContextLiteral() && ps.contextLiteral().constValue() != null) {
                    // constant AVP provider: a fresh context item per action, as before
                    providers.add(toItemProvider(ps, actionSpec.inherited()));
                    continue;
                }
                ItemProvider provider = cache.get(ps);
                if (provider == null) {
                    provider = toItemProvider(ps, actionSpec.inherited());
                    cache.put(ps, provider);
                }
                providers.add(provider);
            }
            return new InterpretationAction(anchor, providers, operation, actionSpec.inherited());
        }

        private ItemProvider toItemProvider(ProviderSpec spec, boolean lenient) {
            if (spec.isContextLiteral()) {
                if (spec.contextLiteral().constValue() != null) {
                    ContextDerivedItem item = new ContextDerivedItem(
                            spec.contextLiteral().text(), ItemType.ATTRIBUTE,
                            spec.contextLiteral().constValue());
                    return new ContextDerivedItemProvider(List.of(item), ContextDerivedProviderKind.UNRESTRICTED);
                }
                ContextDerivedItem item = getOrCreateContextItem(spec.contextLiteral());
                return new ContextDerivedItemProvider(List.of(item), spec.contextLiteral().kind());
            }
            return new CellDerivedItemProvider(
                    new ItemFilter(spec.filterCondition().toCondition()),
                    new ItemLinearization(spec.traversalOrder()),
                    index,
                    CandidateScopes.of(spec.filterCondition()),
                    spec.cardinality(),
                    spec.targetItemKind(),
                    true,     // excludeAnchorFromCandidates (same as 5-arg default)
                    lenient);
        }

        private ContextDerivedItem getOrCreateContextItem(ProviderSpec.ContextLiteralSpec spec) {
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
                case CONCAT -> new ConcatOperation(as.key());
                case JOIN -> new JoinOperation(as.key());
            };
        }
    }

    public static final class MatchException extends RuntimeException {
        public MatchException(String message) {
            super(message);
        }
    }
}
