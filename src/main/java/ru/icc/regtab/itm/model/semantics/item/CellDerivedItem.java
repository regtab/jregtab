package ru.icc.regtab.itm.model.semantics.item;

import ru.icc.regtab.itm.model.semantics.predicate.Has;
import ru.icc.regtab.itm.model.semantics.predicate.Is;
import ru.icc.regtab.itm.model.syntax.Cell;

import java.util.List;
import java.util.Objects;

/**
 * A cell-derived item: a triple (s, u_vec, i) where s is a string obtained from
 * the source cell, u_vec is a sequence of user-defined tags, and i is a zero-based
 * index of the item within the cell (Def. 5).
 */
public final class CellDerivedItem implements Item {

    private final String str;
    private final List<String> tags;
    private final int index;
    private final Cell cell;
    private final ItemType type;
    /** Entry point for fluent predicate checks on this item as candidate (position). */
    public final Is is;
    /** Entry point for fluent predicate checks on this item as candidate (formatting, content). */
    public final Has has;

    public CellDerivedItem(String str, List<String> tags, int index, Cell cell, ItemType type) {
        this.str = Objects.requireNonNull(str, "str");
        this.tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        if (index < 0) throw new IllegalArgumentException("index must be non-negative: " + index);
        this.index = index;
        this.cell = Objects.requireNonNull(cell, "cell");
        this.type = Objects.requireNonNull(type, "type");
        this.is = new Is(this);
        this.has = new Has(this);
    }

    public CellDerivedItem(String str, int index, Cell cell, ItemType type) {
        this(str, List.of(), index, cell, type);
    }

    public boolean sameCol(CellDerivedItem anchor) { return is.in.sameCol(anchor); }
    public boolean sameRow(CellDerivedItem anchor) { return is.in.sameRow(anchor); }
    public boolean sameCell(CellDerivedItem anchor) { return is.in.sameCell(anchor); }
    public boolean sameSubtable(CellDerivedItem anchor) { return is.in.sameSubtable(anchor); }
    public boolean sameSubrow(CellDerivedItem anchor) { return is.in.sameSubrow(anchor); }

    @Override
    public String str() { return str; }

    @Override
    public ItemType type() { return type; }

    public List<String> tags() { return tags; }

    public int index() { return index; }

    public Cell cell() { return cell; }

    @Override
    public String toString() {
        return "CellDerivedItem[str=\"" + str + "\", index=" + index
                + ", cell=" + cell.pos() + ", type=" + type + "]";
    }
}
