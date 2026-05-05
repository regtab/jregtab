package ru.icc.regtab.itm.pattern;

import java.util.List;

/**
 * One token inside a {@link CompoundSplitSpec}: emitted attribute or value, optional rec anchor with providers.
 * {@code O_avp} for compound cells is configured on the {@link CellGroupSpec} via {@code actions().avp(...)}.
 */
public record CompoundTokenSpec(
        boolean emit,
        boolean attributeItem,
        boolean recAnchor,
        List<ProviderSpec> recProviders) {

    public CompoundTokenSpec {
        recProviders = recProviders == null ? List.of() : List.copyOf(recProviders);
    }

    public static CompoundTokenSpec plain() {
        return new CompoundTokenSpec(true, false, false, List.of());
    }

    /** Emits {@link ru.icc.regtab.itm.model.semantics.item.ItemType#ATTRIBUTE} (key before {@code sep}). */
    public static CompoundTokenSpec attributePlain() {
        return new CompoundTokenSpec(true, true, false, List.of());
    }

    /** Value token that is a {@code rec} anchor with the given provider(s). */
    public static CompoundTokenSpec anchorRec(List<ProviderSpec> providers) {
        return new CompoundTokenSpec(true, false, true, providers);
    }

    /** Text after the last separator is consumed but no item is emitted (compound {@code skip}). */
    public static CompoundTokenSpec skip() {
        return new CompoundTokenSpec(false, false, false, List.of());
    }
}
