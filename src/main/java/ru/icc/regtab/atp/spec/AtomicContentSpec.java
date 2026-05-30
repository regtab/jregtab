package ru.icc.regtab.atp.spec;

import java.util.List;
import java.util.Objects;

/**
 * Atomic content specification S_atom (def:atomic-content-spec):
 * S_atom = (idd, ξ, ⟨S_act¹, …, S_actᵐ⟩).
 * <p>
 * Describes how a single item is derived from the input text of the matched cell.
 * The derived item ι_anch serves as the anchor for all action specifications S_act.
 *
 * @param idd       item derivation directive idd (VAL, ATTR, AUX, SKIP)
 * @param extractor optional string extractor ξ (null = identity, i.e. raw input text used directly)
 * @param actions   ordered sequence of action specifications ⟨S_act¹, …, S_actᵐ⟩
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

    /** Convenience: ATTR with extractor and no actions. */
    public static AtomicContentSpec attr(StringExtractor extractor) {
        return new AtomicContentSpec(ItemDerivationDirective.ATTR, extractor, List.of(), List.of());
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
