package ru.icc.regtab.pattern;

import java.util.List;
import java.util.Objects;

/**
 * {@code O_prefix}: anchor item, sequence of cell-derived providers, delimiter δ for joining
 * {@code str(ι)} before the anchor string (see {@link FillSpec} for the same provider shape).
 */
record PrefixSpec(List<ProviderSpec> providers, String delimiter) {
    PrefixSpec {
        Objects.requireNonNull(providers, "providers");
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("prefix requires at least one provider");
        }
        providers = List.copyOf(providers);
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
