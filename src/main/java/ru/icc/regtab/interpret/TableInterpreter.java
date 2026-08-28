package ru.icc.regtab.interpret;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.Diagnostic;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.WorkingState;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.ItemProvider;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.recordset.Schema;

import java.util.*;

/**
 * Table interpreter: derives a recordset from an InterpretableTable
 * by executing 4 phases (Sec. 3.3).
 * <p>
 * Working state completion applies the actions in operation-type order
 * {@code FILL/PREFIX/SUFFIX → AVP → REC → CONCAT → JOIN}: records are folded by concatenation
 * before they are multiplied by joins. Preconditions violated by {@code CONCAT}/{@code JOIN}
 * leave the working state unchanged and are reported through {@link #diagnostics()}
 * (or raise an exception under {@link #withStrictPreconditions(boolean) strict preconditions}).
 */
public final class TableInterpreter {

    private static final String DEFAULT_ANONYMOUS_ATTRIBUTE_TEMPLATE = "$a_%i";

    private SchemaConstructionStrategy strategy = SchemaConstructionStrategy.RECORD_FIRST;
    private ActionApplicationStrategy actionApplicationStrategy = ActionApplicationStrategy.ROW_FIRST;
    private MissingValueHandler missingValueHandler = MissingValueHandler.NULL_HANDLER;
    private List<RecordsetTransformation> transformations = List.of();
    private String anonymousAttributeTemplate = DEFAULT_ANONYMOUS_ATTRIBUTE_TEMPLATE;
    private boolean strictPreconditions = false;
    private List<Diagnostic> diagnostics = List.of();

    public TableInterpreter withStrategy(SchemaConstructionStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
        return this;
    }

    public TableInterpreter withActionApplicationStrategy(ActionApplicationStrategy actionApplicationStrategy) {
        this.actionApplicationStrategy = Objects.requireNonNull(actionApplicationStrategy);
        return this;
    }

    public TableInterpreter withMissingValueHandler(MissingValueHandler handler) {
        this.missingValueHandler = Objects.requireNonNull(handler);
        return this;
    }

    public TableInterpreter withTransformations(List<RecordsetTransformation> transformations) {
        this.transformations = List.copyOf(Objects.requireNonNull(transformations));
        return this;
    }

    /**
     * Sets the template for generating anonymous attribute names.
     * The placeholder {@code %i} is replaced by the positional index.
         * <p>Example: {@code "A%i"} produces {@code "A1"}, {@code "A2"}, etc.
     * <p>Default: {@code "$a_%i"}.
     */
    public TableInterpreter withAnonymousAttributeTemplate(String template) {
        Objects.requireNonNull(template, "template");
        if (!template.contains("%i")) {
            throw new IllegalArgumentException("Template must contain the placeholder %i: " + template);
        }
        this.anonymousAttributeTemplate = template;
        return this;
    }

    /**
     * Strict preconditions: a {@code CONCAT} / {@code JOIN} action whose precondition is violated
     * (e.g. a named attribute shared by two records being concatenated) raises an
     * {@link IllegalStateException} instead of having no effect. Default: {@code false} —
     * by the formal model the operation has no effect, and the violation is reported through
     * {@link #diagnostics()}.
     */
    public TableInterpreter withStrictPreconditions(boolean strict) {
        this.strictPreconditions = strict;
        return this;
    }

    /**
     * Diagnostics of the most recent {@link #interpret(InterpretableTable)} call: every
     * {@code CONCAT} / {@code JOIN} action that was skipped because its precondition was violated.
     * Empty if nothing was skipped (or before the first call).
     */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * Interprets the given table and returns the resulting recordset.
     */
    public Recordset interpret(InterpretableTable table) {
        TableSemantics sem = table.semantics();

        // Phase 1: Working state initialization
        WorkingState ws = initWorkingState(sem);

        // Phase 2: Working state completion
        completeWorkingState(ws, sem.actions());
        diagnostics = List.copyOf(ws.diagnostics());

        // Phase 3: Recordset extraction
        Recordset recordset = extractRecordset(ws);

        // Phase 4: Recordset transformation
        recordset = transformRecordset(recordset);

        return recordset;
    }

    // --- Phase 1: Working state initialization ---

    private WorkingState initWorkingState(TableSemantics sem) {
        WorkingState ws = new WorkingState(strictPreconditions);

        for (CellDerivedItem item : sem.cellDerivedItems()) {
            switch (item.type()) {
                case VALUE -> ws.initVal(item, item.str());
                case ATTRIBUTE -> ws.initAttr(item, item.str());
                case AUXILIARY -> { /* no init needed */ }
            }
        }
        for (ContextDerivedItem item : sem.contextDerivedItems()) {
            switch (item.type()) {
                case VALUE -> ws.initVal(item, item.str());
                case ATTRIBUTE -> ws.initAttr(item, item.str());
                case AUXILIARY -> { /* no init needed */ }
            }
        }
        return ws;
    }

    // --- Phase 2: Working state completion ---

    private void completeWorkingState(WorkingState ws, List<InterpretationAction> actions) {
        List<InterpretationAction> strActions = new ArrayList<>();
        List<InterpretationAction> avpActions = new ArrayList<>();
        List<InterpretationAction> recActions = new ArrayList<>();
        List<InterpretationAction> concatActions = new ArrayList<>();
        List<InterpretationAction> joinActions = new ArrayList<>();

        for (InterpretationAction action : actions) {
            switch (action.operation()) {
                case FillOperation ignored -> strActions.add(action);
                case PrefixOperation ignored -> strActions.add(action);
                case SuffixOperation ignored -> strActions.add(action);
                case AvpOperation ignored -> avpActions.add(action);
                case RecOperation ignored -> recActions.add(action);
                case ConcatOperation ignored -> concatActions.add(action);
                case JoinOperation ignored -> joinActions.add(action);
            }
        }

        Comparator<InterpretationAction> cmp = actionApplicationStrategy.actionComparator();
        strActions.sort(cmp);
        avpActions.sort(cmp);
        recActions.sort(cmp);
        concatActions.sort(cmp);
        joinActions.sort(cmp);

        for (InterpretationAction action : strActions) applyAction(ws, action);
        for (InterpretationAction action : avpActions) applyAction(ws, action);
        for (InterpretationAction action : recActions) applyAction(ws, action);
        for (InterpretationAction action : concatActions) applyAction(ws, action);
        for (InterpretationAction action : joinActions) applyAction(ws, action);
    }

    private void applyAction(WorkingState ws, InterpretationAction action) {
        Item anchor = action.anchor();
        List<? extends Item> items;
        if (action.providers().size() == 1) {
            items = action.providers().getFirst().provide(anchor);
        } else {
            List<Item> collected = new ArrayList<>();
            for (ItemProvider provider : action.providers()) {
                collected.addAll(provider.provide(anchor));
            }
            items = collected;
        }

        switch (action.operation()) {
            case FillOperation op -> ws.applyFill(anchor, items, op.delimiter());
            case PrefixOperation op -> ws.applyPrefix(anchor, items, op.delimiter());
            case SuffixOperation op -> ws.applySuffix(anchor, items, op.delimiter());
            // Empty items (e.g. lenient inherited provider on incompatible anchor) → skip
            case AvpOperation ignored  -> { if (!items.isEmpty()) ws.applyAvp(anchor, items); }
            case RecOperation ignored  -> ws.applyRec((CellDerivedItem) anchor, items);
            case ConcatOperation op -> { if (!items.isEmpty()) ws.applyConcat((CellDerivedItem) anchor, items, op.keyPositions()); }
            case JoinOperation op   -> { if (!items.isEmpty()) ws.applyJoin((CellDerivedItem) anchor, items, op.keyPositions()); }
        }
    }

    // --- Phase 3: Recordset extraction ---

    private Recordset extractRecordset(WorkingState ws) {
        if (!ws.isRecordsetConsistent()) {
            throw new IllegalStateException("Working state is not recordset-consistent");
        }

        Schema schema = constructSchema(ws);
        List<Record> records = generateRecords(ws, schema);

        return new Recordset(schema, records);
    }

    private Schema constructSchema(WorkingState ws) {
        Map<CellDerivedItem, List<List<Item>>> allRec = ws.allRec();      // live anchors only
        List<CellDerivedItem> anchors = new ArrayList<>(allRec.keySet());

        List<String> schemaAttrs = new ArrayList<>();
        Map<Integer, String> anonMap = new HashMap<>();

        String a1 = null;
        for (CellDerivedItem anchor : anchors) {
            String a = ws.assoc(anchor);
            if (a != null) { a1 = a; break; }
        }
        if (a1 == null) {
            a1 = anonymousAttribute(1);
            for (CellDerivedItem anchor : anchors) {
                String v = ws.val(anchor);
                if (v != null) {
                    ws.setAvp(anchor, a1, v);
                }
            }
        }
        schemaAttrs.add(a1);

        List<int[]> triples = strategy.buildVisitOrder(anchors, allRec);
        Set<String> inSchema = new LinkedHashSet<>(schemaAttrs);

        for (int[] triple : triples) {
            CellDerivedItem anchor = anchors.get(triple[0]);
            List<List<Item>> records = allRec.get(anchor);
            if (triple[1] >= records.size()) continue;
            List<Item> sequence = records.get(triple[1]);
            int posIdx = triple[2];
            if (posIdx >= sequence.size()) continue;
            Item item = sequence.get(posIdx);

            String a = ws.assoc(item);
            if (a != null) {
                if (inSchema.add(a)) {
                    schemaAttrs.add(a);
                }
            } else {
                if (!anonMap.containsKey(posIdx)) {
                    String anonAttr = anonymousAttribute(posIdx + 1);
                    anonMap.put(posIdx, anonAttr);
                    schemaAttrs.add(anonAttr);
                    inSchema.add(anonAttr);
                }
                String v = ws.val(item);
                if (v != null) {
                    ws.setAvp(item, anonMap.get(posIdx), v);
                }
            }
        }

        return new Schema(schemaAttrs);
    }

    private String anonymousAttribute(int index) {
        return anonymousAttributeTemplate.replace("%i", Integer.toString(index));
    }

    private List<Record> generateRecords(WorkingState ws, Schema schema) {
        List<Record> records = new ArrayList<>();

        List<String> attrs = schema.attributes();
        for (var entry : ws.allRec().entrySet()) {                       // live anchors only
            for (List<Item> sequence : entry.getValue()) {
                String[] values = new String[attrs.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = missingValueHandler.handle(attrs.get(i));
                }
                for (Item item : sequence) {
                    String a = ws.assoc(item);
                    if (a == null) continue;
                    int i = schema.indexOf(a);
                    if (i >= 0) values[i] = ws.val(item);
                }
                records.add(new Record(schema, values));
            }
        }
        return records;
    }

    // --- Phase 4: Recordset transformation ---

    private Recordset transformRecordset(Recordset recordset) {
        for (RecordsetTransformation t : transformations) {
            recordset = t.withAnonymousAttributeTemplate(anonymousAttributeTemplate).apply(recordset);
        }
        return recordset;
    }

}
