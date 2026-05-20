package ru.icc.regtab.itm.atp.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Interpretation action specification (Def. 19):
 * AS = (op, ⟨PS₁, …, PSₙ⟩).
 * <p>
 * A template from which a concrete {@link ru.icc.regtab.itm.model.semantics.action.InterpretationAction}
 * is constructed at match time, with the derived item bound as the anchor.
 *
 * @param operationType type of working-state update operation
 * @param delimiter     delimiter for FILL/PREFIX/SUFFIX (empty string if none); null for AVP/REC/CONCAT
 * @param providers     sequence of item provider specifications
 */
public record ActionSpec(
        OperationType operationType,
        String delimiter,
        List<ProviderSpec> providers
) {
    public ActionSpec {
        Objects.requireNonNull(operationType, "operationType");
        providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    /** Convenience: REC action with given providers. */
    public static ActionSpec rec(ProviderSpec... providers) {
        return new ActionSpec(OperationType.REC, null, List.of(providers));
    }

    /** Convenience: REC action — all providers share the same cardinality and default traversal. */
    public static ActionSpec rec(int cardinality, ItemFilterConditionSpec... conditions) {
        var providers = new ArrayList<ProviderSpec>(conditions.length);
        for (var c : conditions) {
            providers.add(ProviderSpec.val(cardinality, c));
        }
        return new ActionSpec(OperationType.REC, null, List.copyOf(providers));
    }

    /** Convenience: AVP action with one provider. */
    public static ActionSpec avp(ProviderSpec provider) {
        return new ActionSpec(OperationType.AVP, null, List.of(provider));
    }

    /** Convenience: AVP action with a literal context-attribute provider. */
    public static ActionSpec avp(String literal) {
        return new ActionSpec(OperationType.AVP, null, List.of(ProviderSpec.ctxAttr(literal)));
    }

    /** Convenience: CONCAT action with given providers. */
    public static ActionSpec concat(ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers));
    }

    /** Convenience: FILL action with delimiter and providers. */
    public static ActionSpec fill(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.FILL, delimiter, List.of(providers));
    }

    /** Convenience: PREFIX action with delimiter and providers. */
    public static ActionSpec prefix(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.PREFIX, delimiter, List.of(providers));
    }

    /** Convenience: SUFFIX action with delimiter and providers. */
    public static ActionSpec suffix(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.SUFFIX, delimiter, List.of(providers));
    }
}
