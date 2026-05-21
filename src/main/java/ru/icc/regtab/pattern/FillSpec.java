package ru.icc.regtab.pattern;

import java.util.List;
import java.util.Objects;

/**
 * {@code O_fill}: anchor item, sequence of cell-derived providers (Def.~14 in ITM), delimiter δ for
 * {@code str(ι₁) ⊕_δ … ⊕_δ str(ιₙ)}. Same {@link ProviderSpec} shape as {@code rec}.
 * <p>
 * When the group comes from {@code cells().one().when(...).val().actions().fill/prefix/suffix(...).otherwise().val()},
 * {@link CellGroupSpec#branchWhen()} selects cells that receive fill.
 */
record FillSpec(List<ProviderSpec> providers, String delimiter) {
    FillSpec {
        Objects.requireNonNull(providers, "providers");
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("fill requires at least one provider");
        }
        providers = List.copyOf(providers);
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
