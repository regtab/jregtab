package ru.icc.regtab.interpret;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Schema construction strategy Γ (sec:itm:table-interpretation): defines the
 * order in which (anchor, position) pairs are visited when constructing the schema.
 */
public enum SchemaConstructionStrategy {
    /**
     * Record-first (Γ_rec): iterates over anchors, for each anchor iterates over positions.
     */
    RECORD_FIRST {
        @Override
        public List<int[]> buildVisitOrder(
                List<CellDerivedItem> anchors,
                Map<CellDerivedItem, List<Item>> allRec) {
            List<int[]> pairs = new ArrayList<>();
            for (int a = 0; a < anchors.size(); a++) {
                List<Item> seq = allRec.get(anchors.get(a));
                for (int i = 1; i < seq.size(); i++) {
                    pairs.add(new int[]{a, i});
                }
            }
            return pairs;
        }
    },

    /**
     * Position-first (Γ_pos): iterates over positions, for each position iterates over anchors.
     */
    POSITION_FIRST {
        @Override
        public List<int[]> buildVisitOrder(
                List<CellDerivedItem> anchors,
                Map<CellDerivedItem, List<Item>> allRec) {
            List<int[]> pairs = new ArrayList<>();
            int maxLen = 0;
            for (CellDerivedItem anchor : anchors) {
                maxLen = Math.max(maxLen, allRec.get(anchor).size());
            }
            for (int i = 1; i < maxLen; i++) {
                for (int a = 0; a < anchors.size(); a++) {
                    pairs.add(new int[]{a, i});
                }
            }
            return pairs;
        }
    };

    /**
     * Builds the visit order of (anchorIndex, positionIndex) pairs
     * for schema construction.
     */
    public abstract List<int[]> buildVisitOrder(
            List<CellDerivedItem> anchors,
            Map<CellDerivedItem, List<Item>> allRec);
}
