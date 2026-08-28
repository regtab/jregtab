package ru.icc.regtab.interpret;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Schema construction strategy Γ (sec:itm:table-interpretation): defines the
 * order in which (anchor, record, position) triples are visited when constructing the schema.
 * An anchor may carry several records after a join (the record product); the records of
 * an anchor are always visited in their sequence order.
 */
public enum SchemaConstructionStrategy {
    /**
     * Record-first (Γ_rec): iterates over anchors, for each anchor over its records,
     * for each record over positions.
     */
    RECORD_FIRST {
        @Override
        public List<int[]> buildVisitOrder(
                List<CellDerivedItem> anchors,
                Map<CellDerivedItem, List<List<Item>>> allRec) {
            List<int[]> triples = new ArrayList<>();
            for (int a = 0; a < anchors.size(); a++) {
                List<List<Item>> records = allRec.get(anchors.get(a));
                for (int r = 0; r < records.size(); r++) {
                    List<Item> seq = records.get(r);
                    for (int i = 1; i < seq.size(); i++) {
                        triples.add(new int[]{a, r, i});
                    }
                }
            }
            return triples;
        }
    },

    /**
     * Position-first (Γ_pos): iterates over positions, for each position over anchors
     * and their records.
     */
    POSITION_FIRST {
        @Override
        public List<int[]> buildVisitOrder(
                List<CellDerivedItem> anchors,
                Map<CellDerivedItem, List<List<Item>>> allRec) {
            List<int[]> triples = new ArrayList<>();
            int maxLen = 0;
            for (CellDerivedItem anchor : anchors) {
                for (List<Item> seq : allRec.get(anchor)) {
                    maxLen = Math.max(maxLen, seq.size());
                }
            }
            for (int i = 1; i < maxLen; i++) {
                for (int a = 0; a < anchors.size(); a++) {
                    List<List<Item>> records = allRec.get(anchors.get(a));
                    for (int r = 0; r < records.size(); r++) {
                        triples.add(new int[]{a, r, i});
                    }
                }
            }
            return triples;
        }
    };

    /**
     * Builds the visit order of (anchorIndex, recordIndex, positionIndex) triples
     * for schema construction. Positions beyond the end of a record are skipped by the caller.
     */
    public abstract List<int[]> buildVisitOrder(
            List<CellDerivedItem> anchors,
            Map<CellDerivedItem, List<List<Item>>> allRec);
}
