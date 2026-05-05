package ru.icc.regtab.itm.interpret;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.TableSemantics;
import ru.icc.regtab.itm.model.semantics.WorkingState;
import ru.icc.regtab.itm.model.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.Item;
import ru.icc.regtab.itm.model.semantics.operation.*;
import ru.icc.regtab.itm.model.semantics.provider.ItemProvider;
import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.recordset.Schema;

import java.util.*;

/**
 * Table interpreter: derives a recordset from an InterpretableTable
 * by executing 4 phases (Sec. 3.3).
 */
public final class TableInterpreter {

    private static final String DEFAULT_ANONYMOUS_ATTRIBUTE_TEMPLATE = "$a_%i";

    private SchemaConstructionStrategy strategy = SchemaConstructionStrategy.RECORD_FIRST;
    private ActionApplicationStrategy actionApplicationStrategy = ActionApplicationStrategy.ROW_FIRST;
    private MissingValueHandler missingValueHandler = MissingValueHandler.NULL_HANDLER;
    private List<RecordsetTransformation> transformations = List.of();
    private String anonymousAttributeTemplate = DEFAULT_ANONYMOUS_ATTRIBUTE_TEMPLATE;

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
     * Interprets the given table and returns the resulting recordset.
     */
    public Recordset interpret(InterpretableTable table) {
        TableSemantics sem = table.semantics();

        // Phase 1: Working state initialization
        WorkingState ws = initWorkingState(sem);

        // Phase 2: Working state completion
        completeWorkingState(ws, sem.actions());

        // Phase 3: Recordset extraction
        Recordset recordset = extractRecordset(ws);

        // Phase 4: Recordset transformation
        recordset = transformRecordset(recordset);

        return recordset;
    }

    // --- Phase 1: Working state initialization ---

    private WorkingState initWorkingState(TableSemantics sem) {
        WorkingState ws = new WorkingState();

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

        for (InterpretationAction action : actions) {
            switch (action.operation()) {
                case FillOperation _, PrefixOperation _, SuffixOperation _ -> strActions.add(action);
                case AvpOperation _ -> avpActions.add(action);
                case RecOperation _ -> recActions.add(action);
                case ConcatOperation _ -> concatActions.add(action);
            }
        }

        Comparator<InterpretationAction> cmp = actionApplicationStrategy.actionComparator();
        strActions.sort(cmp);
        avpActions.sort(cmp);
        recActions.sort(cmp);
        concatActions.sort(cmp);

        for (InterpretationAction action : strActions) applyAction(ws, action);
        for (InterpretationAction action : avpActions) applyAction(ws, action);
        for (InterpretationAction action : recActions) applyAction(ws, action);
        for (InterpretationAction action : concatActions) applyAction(ws, action);
    }

    private void applyAction(WorkingState ws, InterpretationAction action) {
        Item anchor = action.anchor();
        List<Item> items = new ArrayList<>();
        for (ItemProvider provider : action.providers()) {
            items.addAll(provider.provide(anchor));
        }

        switch (action.operation()) {
            case FillOperation op -> ws.applyFill(anchor, items, op.delimiter());
            case PrefixOperation op -> ws.applyPrefix(anchor, items, op.delimiter());
            case SuffixOperation op -> ws.applySuffix(anchor, items, op.delimiter());
            case AvpOperation _ -> ws.applyAvp(anchor, items);
            case RecOperation _ -> ws.applyRec((CellDerivedItem) anchor, items);
            case ConcatOperation _ -> ws.applyConcat((CellDerivedItem) anchor, items);
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
        Map<CellDerivedItem, List<Item>> allRec = ws.allRec();
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

        List<int[]> pairs = strategy.buildVisitOrder(anchors, allRec);
        Set<String> inSchema = new LinkedHashSet<>(schemaAttrs);

        for (int[] pair : pairs) {
            CellDerivedItem anchor = anchors.get(pair[0]);
            int posIdx = pair[1];
            List<Item> sequence = allRec.get(anchor);
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

        for (var entry : ws.allRec().entrySet()) {
            Map<String, String> values = new LinkedHashMap<>();
            for (String attr : schema.attributes()) {
                values.put(attr, missingValueHandler.handle(attr));
            }
            for (Item item : entry.getValue()) {
                String a = ws.assoc(item);
                if (a != null && schema.contains(a)) {
                    values.put(a, ws.val(item));
                }
            }
            records.add(new Record(schema, values));
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
