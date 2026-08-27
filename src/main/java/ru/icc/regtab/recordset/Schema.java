package ru.icc.regtab.recordset;

import java.util.*;

/**
 * A schema: an ordered sequence of unique attribute names.
 */
public final class Schema {

    private final List<String> attributes;
    private final Set<String> attributeSet;
    private final Map<String, Integer> index;

    public Schema(List<String> attributes) {
        Objects.requireNonNull(attributes, "attributes");
        Set<String> seen = new HashSet<>();
        for (String attr : attributes) {
            Objects.requireNonNull(attr, "attribute must not be null");
            if (!seen.add(attr)) {
                throw new IllegalArgumentException("Duplicate attribute: " + attr);
            }
        }
        this.attributes = List.copyOf(attributes);
        this.attributeSet = Set.copyOf(seen);
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < this.attributes.size(); i++) idx.put(this.attributes.get(i), i);
        this.index = idx;
    }

    public List<String> attributes() { return attributes; }

    public int size() { return attributes.size(); }

    public int indexOf(String attribute) { Integer i = index.get(attribute); return i == null ? -1 : i; }

    public boolean contains(String attribute) { return attributeSet.contains(attribute); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Schema s && attributes.equals(s.attributes));
    }

    @Override
    public int hashCode() { return attributes.hashCode(); }

    @Override
    public String toString() { return "Schema" + attributes; }
}
