package ru.icc.regtab.itm.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Atomic content specification (Def. 20):
 * CS_atom = (idd, ξ, ⟨AS₁, …, ASₘ⟩).
 * <p>
 * Describes how a single item is derived from the input text of the matched cell.
 * The derived item serves as the anchor for all action specifications.
 *
 * @param idd       item derivation directive (VAL, ATTR, AUX, SKIP)
 * @param extractor optional string extractor ξ (null = identity)
 * @param actions   ordered sequence of interpretation action specifications
 */
public record AtomicContentSpec(
        ItemDerivationDirective idd,
        StringExtractor extractor,
        List<String> tags,
        List<ActionSpec> actions
) implements ContentSpec {

    public AtomicContentSpec {
        Objects.requireNonNull(idd, "idd");
        tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        actions = List.copyOf(Objects.requireNonNull(actions, "actions"));
    }

    /** Convenience: VAL with no extractor and no actions. */
    public static AtomicContentSpec val() {
        return new AtomicContentSpec(ItemDerivationDirective.VAL, null, List.of(), List.of());
    }

    /** Convenience: ATTR with no extractor and no actions. */
    public static AtomicContentSpec attr() {
        return new AtomicContentSpec(ItemDerivationDirective.ATTR, null, List.of(), List.of());
    }

    /** Convenience: AUX with no extractor and no actions. */
    public static AtomicContentSpec aux() {
        return new AtomicContentSpec(ItemDerivationDirective.AUX, null, List.of(), List.of());
    }

    /** Convenience: SKIP. */
    public static AtomicContentSpec skip() {
        return new AtomicContentSpec(ItemDerivationDirective.SKIP, null, List.of(), List.of());
    }

    /** Convenience: VAL with actions. */
    public static AtomicContentSpec val(ActionSpec... actions) {
        return new AtomicContentSpec(ItemDerivationDirective.VAL, null, List.of(), List.of(actions));
    }

    /** Convenience: ATTR with actions. */
    public static AtomicContentSpec attr(ActionSpec... actions) {
        return new AtomicContentSpec(ItemDerivationDirective.ATTR, null, List.of(), List.of(actions));
    }

    /** Convenience: VAL with extractor and actions. */
    public static AtomicContentSpec val(StringExtractor extractor, ActionSpec... actions) {
        return new AtomicContentSpec(ItemDerivationDirective.VAL, extractor, List.of(), List.of(actions));
    }

    /** Convenience: VAL with tags and actions. */
    public static AtomicContentSpec valTagged(String tag, ActionSpec... actions) {
        return new AtomicContentSpec(ItemDerivationDirective.VAL, null, List.of(tag), List.of(actions));
    }

    /** Convenience: VAL with multiple tags and actions. */
    public static AtomicContentSpec valTagged(List<String> tags, ActionSpec... actions) {
        return new AtomicContentSpec(ItemDerivationDirective.VAL, null, tags, List.of(actions));
    }
}
