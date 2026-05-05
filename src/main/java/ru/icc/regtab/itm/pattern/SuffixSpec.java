package ru.icc.regtab.itm.pattern;

import java.util.List;
import java.util.Objects;

/**
 * {@code O_suffix}: anchor item, sequence of cell-derived providers, delimiter δ for joining
 * {@code str(ι)} after the anchor string (see {@link FillSpec} for the same provider shape).
 */
record SuffixSpec(List<ProviderSpec> providers, String delimiter) {
    SuffixSpec {
        Objects.requireNonNull(providers, "providers");
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("suffix requires at least one provider");
        }
        providers = List.copyOf(providers);
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
