package ru.icc.regtab.itm.semantics;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.operation.RecordKey;

import java.util.*;

/**
 * Working state of an ITM instance (def:working-state):
 * {@code ws = (V, A, val, attr, avp, rec, J)}.
 * Tracks values, attributes, attribute-value pairs, and item-based records
 * as they are built up during table interpretation.
 * <p>
 * {@code rec} maps an anchor item to a <em>non-empty sequence</em> of item-based records:
 * a single record after {@code O_rec}/{@code O_concat}, several after {@code O_join}
 * (the record product). {@code J} — the <em>joined-away anchors</em> — are items whose records
 * have been consumed by a join; they stay in {@code rec} (a later join may consume the same
 * records again, irrespective of action order) but are excluded from recordset extraction.
 * {@link #allRec()} therefore returns the <em>live</em> anchors {@code dom(rec) \ J} only.
 */
public final class WorkingState {

    // Items have identity semantics; iteration order of val/attr/avp is never observable in the
    // result, so open-addressing identity maps keep the per-item footprint small on large tables.
    private final Map<Item, String> val = new IdentityHashMap<>();
    private final Map<Item, String> attr = new IdentityHashMap<>();
    private final Map<Item, AttributeValuePair> avp = new IdentityHashMap<>();
    /** Insertion order of rec defines the order of records — keep it. */
    private final Map<CellDerivedItem, List<List<Item>>> rec = new LinkedHashMap<>();
    /** J: joined-away anchors (identity semantics, like the items themselves). */
    private final Set<CellDerivedItem> joined = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Preconditions violated during completion; the operations had no effect. */
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final boolean strictPreconditions;

    public WorkingState() {
        this(false);
    }

    /**
     * @param strictPreconditions if {@code true}, a violated precondition of {@code O_concat} /
     *                            {@code O_join} raises an {@link IllegalStateException} instead of
     *                            being recorded as a {@link Diagnostic} with no effect
     */
    public WorkingState(boolean strictPreconditions) {
        this.strictPreconditions = strictPreconditions;
    }

    // --- Accessors ---

    public String val(Item item) { return val.get(item); }
    public String attr(Item item) { return attr.get(item); }
    public AttributeValuePair avp(Item item) { return avp.get(item); }

    /** rec(ι): the records of the anchor, also for joined-away anchors; {@code null} if ι ∉ dom(rec). */
    public List<List<Item>> rec(CellDerivedItem item) {
        List<List<Item>> records = rec.get(item);
        return records == null ? null : Collections.unmodifiableList(records);
    }

    public boolean hasVal(Item item) { return val.containsKey(item); }
    public boolean hasAttr(Item item) { return attr.containsKey(item); }
    public boolean hasAvp(Item item) { return avp.containsKey(item); }
    public boolean hasRec(CellDerivedItem item) { return rec.containsKey(item); }
    /** ι ∈ J. */
    public boolean isJoined(CellDerivedItem item) { return joined.contains(item); }

    public Map<Item, String> allVal() { return Collections.unmodifiableMap(val); }
    public Map<Item, String> allAttr() { return Collections.unmodifiableMap(attr); }
    public Map<Item, AttributeValuePair> allAvp() { return Collections.unmodifiableMap(avp); }

    /**
     * The records of the <em>live</em> anchors, {@code dom(rec) \ J}, in insertion order —
     * exactly what recordset extraction sees. Joined-away anchors are reachable through
     * {@link #rec(CellDerivedItem)} and {@link #allJoined()}.
     */
    public Map<CellDerivedItem, List<List<Item>>> allRec() {
        if (joined.isEmpty()) return Collections.unmodifiableMap(rec);
        Map<CellDerivedItem, List<List<Item>>> live = new LinkedHashMap<>();
        for (var entry : rec.entrySet()) {
            if (!joined.contains(entry.getKey())) live.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(live);
    }

    /** J: the joined-away anchors. */
    public Set<CellDerivedItem> allJoined() { return Collections.unmodifiableSet(joined); }

    /** Preconditions violated so far (the corresponding operations had no effect). */
    public List<Diagnostic> diagnostics() { return Collections.unmodifiableList(diagnostics); }

    /**
     * Derived function: assoc(iota) = a iff avp(iota) = (a, v).
     */
    public String assoc(Item item) {
        AttributeValuePair pair = avp.get(item);
        return pair != null ? pair.attribute() : null;
    }

    // --- Initialization ---

    public void initVal(Item item, String value) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(value);
        val.put(item, value);
    }

    public void initAttr(Item item, String attribute) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(attribute);
        attr.put(item, attribute);
    }

    // --- O_fill: f(anchor) := str(i1) +d ... +d str(in) ---

    public void applyFill(Item anchor, List<? extends Item> items, String delimiter) {
        String joined = joinStrings(items, delimiter);
        setValOrAttr(anchor, joined);
    }

    // --- O_prefix: f(anchor) := str(i1) +d ... +d str(in) +d f(anchor) ---

    public void applyPrefix(Item anchor, List<? extends Item> items, String delimiter) {
        if (items.isEmpty()) return;
        String current = getValOrAttr(anchor);
        String prefix = joinStrings(items, delimiter);
        setValOrAttr(anchor, prefix + delimiter + current);
    }

    // --- O_suffix: f(anchor) := f(anchor) +d str(i1) +d ... +d str(in) ---

    public void applySuffix(Item anchor, List<? extends Item> items, String delimiter) {
        if (items.isEmpty()) return;
        String current = getValOrAttr(anchor);
        String suffix = joinStrings(items, delimiter);
        setValOrAttr(anchor, current + delimiter + suffix);
    }

    // --- O_avp: avp(anchor) := (attr(i1), val(anchor)) ---

    public void applyAvp(Item anchor, List<? extends Item> items) {
        if (avp.containsKey(anchor)) return;
        if (items.size() != 1) {
            throw new IllegalArgumentException("O_avp requires exactly 1 item, got: " + items.size());
        }
        Item attrItem = items.getFirst();
        String a = attr.get(attrItem);
        if (a == null) {
            throw new IllegalStateException("No attribute for item: " + attrItem);
        }
        String v = val.get(anchor);
        if (v == null) {
            throw new IllegalStateException("No value for anchor: " + anchor);
        }
        avp.put(anchor, new AttributeValuePair(a, v));
    }

    // --- O_rec: rec(anchor) := <<anchor, i1, ..., in>> ---

    public void applyRec(CellDerivedItem anchor, List<? extends Item> items) {
        if (!val.containsKey(anchor)) return;
        if (rec.containsKey(anchor)) return;
        List<Item> sequence = new ArrayList<>();
        sequence.add(anchor);
        for (Item item : items) {
            if (item instanceof ContextDerivedItem cdi && cdi.constValue() != null) {
                val.put(cdi, cdi.constValue());
                avp.put(cdi, new AttributeValuePair(cdi.str(), cdi.constValue()));
            }
            sequence.add(item);
        }
        List<List<Item>> records = new ArrayList<>(1);
        records.add(sequence);
        rec.put(anchor, records);
    }

    // --- O_concat^K: rec(anchor) := <rho_anch · drop_K(rho_1) · ... · drop_K(rho_n)>; rec.remove(i_k) ---

    /**
     * Concatenates the records of the provided anchors to the anchor's record (one wide record)
     * and removes them from dom(rec). Applicable iff (i) the anchor and at least one provided
     * item have records, (ii) all records agree on the key K (key positions and/or key attribute
     * names, see {@link RecordKey}), and (iii) apart from the key no named attribute occurs in
     * more than one of the concatenated records; otherwise the operation has no effect and a
     * {@link Diagnostic} is recorded.
     */
    public void applyConcat(CellDerivedItem anchor, List<? extends Item> items, RecordKey key) {
        List<List<Item>> anchorRecs = rec.get(anchor);
        if (anchorRecs == null || items.isEmpty()) return;
        List<CellDerivedItem> others = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof CellDerivedItem cdi && cdi != anchor && rec.containsKey(cdi) && !others.contains(cdi)) {
                others.add(cdi);
            }
        }
        if (others.isEmpty()) return;                                   // (i)

        List<Item> anchorRec = anchorRecs.getFirst();
        List<Item> result = new ArrayList<>(anchorRec);
        for (CellDerivedItem other : others) {
            List<Item> otherRec = rec.get(other).getFirst();
            String problem = keyMismatch(anchorRec, otherRec, key);            // (ii)
            if (problem != null) {
                skip(anchor, "CONCAT", problem);
                return;
            }
            result.addAll(dropK(otherRec, key));
        }
        String duplicate = duplicateAttribute(result);                  // (iii)
        if (duplicate != null) {
            skip(anchor, "CONCAT", "named attribute '" + duplicate
                    + "' occurs in more than one of the concatenated records");
            return;
        }
        List<List<Item>> records = new ArrayList<>(1);
        records.add(result);
        rec.put(anchor, records);
        for (CellDerivedItem other : others) {
            rec.remove(other);
            joined.remove(other);
        }
    }

    // --- O_join^K: rec(anchor) := <dedup(rho_i · drop_K(rho'_j)) : compat_K ∧ agree>; J := J ∪ {i_k} ---

    /**
     * Multiplies every record of the anchor by every record of the provided anchors
     * (a cross product for K = ∅, an equi-join on the key K — key positions and/or key attribute
     * names, see {@link RecordKey} — otherwise; a named attribute shared by two records acts as a
     * natural-join condition) and marks the provided anchors as joined-away. Pairs whose keys
     * differ or whose shared attributes disagree are dropped; if no pair survives, the anchor
     * keeps its records (left outer join).
     */
    public void applyJoin(CellDerivedItem anchor, List<? extends Item> items, RecordKey key) {
        List<List<Item>> anchorRecs = rec.get(anchor);
        if (anchorRecs == null || items.isEmpty()) return;
        List<CellDerivedItem> others = new ArrayList<>();
        List<List<Item>> joinedRecs = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof CellDerivedItem cdi && cdi != anchor && rec.containsKey(cdi) && !others.contains(cdi)) {
                others.add(cdi);
                joinedRecs.addAll(rec.get(cdi));
            }
        }
        if (others.isEmpty()) return;

        List<List<Item>> result = new ArrayList<>();
        int dropped = 0;
        for (List<Item> rho : anchorRecs) {
            for (List<Item> rho2 : joinedRecs) {
                if (keyMismatch(rho, rho2, key) != null || !agree(rho, rho2)) {
                    dropped++;
                    continue;
                }
                List<Item> combined = new ArrayList<>(rho);
                combined.addAll(dropK(rho2, key));
                result.add(dedup(combined));
            }
        }
        if (result.isEmpty()) {
            skip(anchor, "JOIN", "none of the " + dropped + " record pairs satisfies the key/attribute conditions; "
                    + "the anchor keeps its records");
        } else {
            rec.put(anchor, result);
        }
        joined.addAll(others);
    }

    /**
     * drop_K(ρ̄): returns the sequence with the key items removed — the items at the key positions
     * (0-based) and the items carrying a key attribute name; the latter are resolved per record.
     */
    private List<Item> dropK(List<Item> sequence, RecordKey key) {
        if (key.isEmpty()) return new ArrayList<>(sequence);
        List<Item> result = new ArrayList<>(sequence.size());
        for (int i = 0; i < sequence.size(); i++) {
            Item item = sequence.get(i);
            if (key.positions().contains(i)) continue;
            String a = assoc(item);
            if (a != null && key.names().contains(a)) continue;
            result.add(item);
        }
        return result;
    }

    /** The index of the first item of the record carrying the named attribute, or -1. */
    private int indexOfAttribute(List<Item> sequence, String attribute) {
        for (int i = 0; i < sequence.size(); i++) {
            if (attribute.equals(assoc(sequence.get(i)))) return i;
        }
        return -1;
    }

    /** dedup(ρ̄): keeps first occurrence of each named attribute; items without avp always kept. */
    private List<Item> dedup(List<Item> sequence) {
        Set<String> seen = new LinkedHashSet<>();
        List<Item> result = new ArrayList<>(sequence.size());
        for (Item item : sequence) {
            String a = assoc(item);
            if (a == null || seen.add(a)) result.add(item);
        }
        return result;
    }

    /**
     * compat_K(ρ, ρ'): {@code null} if for every key position k both records are long enough and the
     * items at k are compatible (both with the same attribute-value pair, or both unnamed with the
     * same value), and for every key attribute name both records carry the name with the same
     * attribute-value pair; otherwise a description of the first mismatch.
     */
    private String keyMismatch(List<Item> rho, List<Item> rho2, RecordKey key) {
        for (int k : key.positions()) {
            if (k >= rho.size() || k >= rho2.size()) {
                return "key position " + k + " is beyond the end of a record";
            }
            Item a = rho.get(k), b = rho2.get(k);
            AttributeValuePair pa = avp.get(a), pb = avp.get(b);
            if (pa != null && pb != null) {
                if (!pa.equals(pb)) return "key position " + k + " differs: " + pa + " vs " + pb;
            } else if (pa == null && pb == null) {
                if (!Objects.equals(val.get(a), val.get(b)))
                    return "key position " + k + " differs: '" + val.get(a) + "' vs '" + val.get(b) + "'";
            } else {
                return "key position " + k + " mixes a named and an unnamed item";
            }
        }
        for (String name : key.names()) {
            int i = indexOfAttribute(rho, name), j = indexOfAttribute(rho2, name);
            if (i < 0 || j < 0) {
                return "key attribute '" + name + "' is missing in a record";
            }
            AttributeValuePair pa = avp.get(rho.get(i)), pb = avp.get(rho2.get(j));
            if (!pa.equals(pb)) return "key attribute '" + name + "' differs: " + pa + " vs " + pb;
        }
        return null;
    }

    /** agree(ρ, ρ'): every named attribute appearing in both records carries the same value. */
    private boolean agree(List<Item> rho, List<Item> rho2) {
        Map<String, String> named = new HashMap<>();
        for (Item item : rho) {
            AttributeValuePair p = avp.get(item);
            if (p != null) named.putIfAbsent(p.attribute(), p.value());
        }
        for (Item item : rho2) {
            AttributeValuePair p = avp.get(item);
            if (p == null) continue;
            String v = named.get(p.attribute());
            if (v != null && !v.equals(p.value())) return false;
        }
        return true;
    }

    /** The first named attribute occurring twice in the sequence, or {@code null}. */
    private String duplicateAttribute(List<Item> sequence) {
        Set<String> seen = new HashSet<>();
        for (Item item : sequence) {
            String a = assoc(item);
            if (a != null && !seen.add(a)) return a;
        }
        return null;
    }

    private void skip(CellDerivedItem anchor, String operation, String message) {
        Diagnostic d = new Diagnostic(anchor, operation, message);
        diagnostics.add(d);
        if (strictPreconditions) throw new IllegalStateException(d.toString());
    }

    // --- Consistency checks ---

    /**
     * For every iota in dom(rec) and every record of it: record[0] == iota.
     */
    public boolean isRecAnchored() {
        for (var entry : rec.entrySet()) {
            for (List<Item> record : entry.getValue()) {
                if (record.isEmpty() || record.getFirst() != entry.getKey()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * For every iota in dom(avp): avp(iota) = (a, v) implies v == val(iota).
     */
    public boolean isAvpValueMatch() {
        for (var entry : avp.entrySet()) {
            String v = val.get(entry.getKey());
            if (!Objects.equals(entry.getValue().value(), v)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Composite consistency: isRecAnchored() && isAvpValueMatch().
     */
    public boolean isConsistent() {
        return isRecAnchored() && isAvpValueMatch();
    }

    /**
     * All live anchors in dom(rec) \ J either have no associated attribute,
     * or share the same attribute.
     */
    public boolean isAnchorAttributeUniform() {
        String commonAttr = null;
        boolean found = false;
        for (CellDerivedItem anchor : rec.keySet()) {
            if (joined.contains(anchor)) continue;
            String a = assoc(anchor);
            if (a != null) {
                if (!found) {
                    commonAttr = a;
                    found = true;
                } else if (!a.equals(commonAttr)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * For every item-based record of every live anchor, the attributes of items
     * that have associated attributes are pairwise distinct.
     */
    public boolean isRecordAttributesDistinct() {
        for (var entry : rec.entrySet()) {
            if (joined.contains(entry.getKey())) continue;
            for (List<Item> record : entry.getValue()) {
                if (duplicateAttribute(record) != null) return false;
            }
        }
        return true;
    }

    /**
     * Composite recordset-consistency: isAnchorAttributeUniform() && isRecordAttributesDistinct().
     */
    public boolean isRecordsetConsistent() {
        return isAnchorAttributeUniform() && isRecordAttributesDistinct();
    }

    // --- Direct manipulation (used by schema construction, Algorithm 1) ---

    /**
     * Directly sets an attribute-value pair for the given item.
     * Used during schema construction for anonymous attributes.
     */
    public void setAvp(Item item, String attribute, String value) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        avp.put(item, new AttributeValuePair(attribute, value));
    }

    // --- Helpers ---

    private String getValOrAttr(Item anchor) {
        if (anchor.type() == ItemType.VALUE) {
            String v = val.get(anchor);
            if (v == null) throw new IllegalStateException("No value for: " + anchor);
            return v;
        } else if (anchor.type() == ItemType.ATTRIBUTE) {
            String a = attr.get(anchor);
            if (a == null) throw new IllegalStateException("No attribute for: " + anchor);
            return a;
        }
        throw new IllegalArgumentException("String ops require VALUE or ATTRIBUTE anchor, got: " + anchor.type());
    }

    private void setValOrAttr(Item anchor, String value) {
        if (anchor.type() == ItemType.VALUE) {
            val.put(anchor, value);
        } else if (anchor.type() == ItemType.ATTRIBUTE) {
            attr.put(anchor, value);
        } else {
            throw new IllegalArgumentException("String ops require VALUE or ATTRIBUTE anchor, got: " + anchor.type());
        }
    }

    private static String joinStrings(List<? extends Item> items, String delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Item item : items) {
            joiner.add(item.str());
        }
        return joiner.toString();
    }

    /**
     * Attribute-value pair: (attribute, value).
     */
    public record AttributeValuePair(String attribute, String value) {
        public AttributeValuePair {
            Objects.requireNonNull(attribute, "attribute");
            Objects.requireNonNull(value, "value");
        }
    }
}
