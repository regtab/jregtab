package ru.icc.regtab.itm.semantics;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.item.ItemType;

import java.util.*;

/**
 * Working state of an ITM instance (def:working-state).
 * Tracks values, attributes, attribute-value pairs, and item-based records
 * as they are built up during table interpretation.
 */
public final class WorkingState {

    private final Map<Item, String> val = new LinkedHashMap<>();
    private final Map<Item, String> attr = new LinkedHashMap<>();
    private final Map<Item, AttributeValuePair> avp = new LinkedHashMap<>();
    private final Map<CellDerivedItem, List<Item>> rec = new LinkedHashMap<>();

    // --- Accessors ---

    public String val(Item item) { return val.get(item); }
    public String attr(Item item) { return attr.get(item); }
    public AttributeValuePair avp(Item item) { return avp.get(item); }
    public List<Item> rec(CellDerivedItem item) { return rec.get(item); }

    public boolean hasVal(Item item) { return val.containsKey(item); }
    public boolean hasAttr(Item item) { return attr.containsKey(item); }
    public boolean hasAvp(Item item) { return avp.containsKey(item); }
    public boolean hasRec(CellDerivedItem item) { return rec.containsKey(item); }

    public Map<Item, String> allVal() { return Collections.unmodifiableMap(val); }
    public Map<Item, String> allAttr() { return Collections.unmodifiableMap(attr); }
    public Map<Item, AttributeValuePair> allAvp() { return Collections.unmodifiableMap(avp); }
    public Map<CellDerivedItem, List<Item>> allRec() { return Collections.unmodifiableMap(rec); }

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

    // --- O_rec: rec(anchor) := <anchor, i1, ..., in> ---

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
        rec.put(anchor, sequence);
    }

    // --- O_join^K: rec(anchor) := dedup(rec(anchor) · drop_K(rec(i1)) · ... · drop_K(rec(in))) ---

    public void applyJoin(CellDerivedItem anchor, List<? extends Item> items, Set<Integer> keyPositions) {
        List<Item> anchorRec = rec.get(anchor);
        if (anchorRec == null || items.isEmpty()) return;
        List<Item> result = new ArrayList<>(anchorRec);
        for (Item item : items) {
            if (!(item instanceof CellDerivedItem cdi)) continue;
            List<Item> otherRec = rec.get(cdi);
            if (otherRec == null) continue;
            result.addAll(dropK(otherRec, keyPositions));
            rec.remove(cdi);
        }
        rec.put(anchor, dedup(result));
    }

    /** drop_K(ρ̄): returns sequence with items at positions k ∈ K removed (0-based). */
    private static List<Item> dropK(List<Item> sequence, Set<Integer> keyPositions) {
        if (keyPositions.isEmpty()) return new ArrayList<>(sequence);
        List<Item> result = new ArrayList<>(sequence.size());
        for (int i = 0; i < sequence.size(); i++) {
            if (!keyPositions.contains(i)) result.add(sequence.get(i));
        }
        return result;
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

    // --- Consistency checks ---

    /**
     * For every iota in dom(rec): rec(iota)[0] == iota.
     */
    public boolean isRecAnchored() {
        for (var entry : rec.entrySet()) {
            if (entry.getValue().isEmpty() || entry.getValue().getFirst() != entry.getKey()) {
                return false;
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
     * All anchors in dom(rec) either have no associated attribute,
     * or share the same attribute.
     */
    public boolean isAnchorAttributeUniform() {
        String commonAttr = null;
        boolean found = false;
        for (CellDerivedItem anchor : rec.keySet()) {
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
     * For every item-based record, the attributes of items
     * that have associated attributes are pairwise distinct.
     */
    public boolean isRecordAttributesDistinct() {
        for (List<Item> sequence : rec.values()) {
            Set<String> seen = new HashSet<>();
            for (Item item : sequence) {
                String a = assoc(item);
                if (a != null && !seen.add(a)) {
                    return false;
                }
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
