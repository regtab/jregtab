package ru.icc.regtab.itm.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A row of the table, consisting of one or more consecutive subrows.
 * Each row belongs to exactly one subtable.
 */
public final class Row {

    private final int index;
    private final List<Subrow> subrows = new ArrayList<>();
    private Subtable subtable;

    public Row(int index) {
        if (index < 0) throw new IllegalArgumentException("index must be non-negative: " + index);
        this.index = index;
    }

    public int index() { return index; }
    public List<Subrow> subrows() { return Collections.unmodifiableList(subrows); }
    public Subtable subtable() { return subtable; }

    void setSubtable(Subtable subtable) { this.subtable = subtable; }

    void addSubrow(Subrow subrow) {
        subrows.add(subrow);
    }

    void clearSubrows() {
        subrows.clear();
    }
}
