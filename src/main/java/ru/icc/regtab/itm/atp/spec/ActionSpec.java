package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.model.semantics.provider.CellDerivedProviderKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Interpretation action specification (Def. 19):
 * AS = (op, ⟨PS₁, …, PSₙ⟩).
 * <p>
 * A template from which a concrete {@link ru.icc.regtab.itm.model.semantics.action.InterpretationAction}
 * is constructed at match time, with the derived item bound as the anchor.
 * <p>
 * Inline REC parameters mirror RTL {@code REC(n)} / {@code REC('/')} syntax:
 * {@link #anchorPos} maps to {@link ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition},
 * {@link #splitDelimiter} maps to {@link ru.icc.regtab.itm.interpret.DelimitedFieldSplit}.
 * {@link TablePattern#of(SubtablePattern...)} collects these automatically.
 *
 * @param operationType  type of working-state update operation
 * @param delimiter      delimiter for FILL/PREFIX/SUFFIX (empty string if none); null for AVP/REC/CONCAT
 * @param providers      sequence of item provider specifications
 * @param anchorPos      inline anchor position for REC (null = none)
 * @param splitDelimiter inline split delimiter for REC (null = none)
 */
public record ActionSpec(
        OperationType operationType,
        String delimiter,
        List<ProviderSpec> providers,
        Integer anchorPos,
        String splitDelimiter
) {
    public ActionSpec {
        Objects.requireNonNull(operationType, "operationType");
        providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        if (operationType == OperationType.REC
                || operationType == OperationType.CONCAT
                || operationType == OperationType.AVP) {
            for (var p : providers) {
                if (!p.isContextLiteral()
                        && p.targetItemKind() == CellDerivedProviderKind.UNRESTRICTED) {
                    throw new IllegalArgumentException(
                            operationType + " action requires a typed provider (VAL/ATTR/AUX), got UNRESTRICTED");
                }
            }
        }
    }

    /** Convenience: REC action with given providers, no inline params. */
    public static ActionSpec rec(ProviderSpec... providers) {
        return new ActionSpec(OperationType.REC, null, List.of(providers), null, null);
    }

    /** Convenience: REC action with inline anchor position (mirrors RTL {@code REC(n)}). */
    public static ActionSpec rec(int anchorPos, ProviderSpec... providers) {
        return new ActionSpec(OperationType.REC, null, List.of(providers), anchorPos, null);
    }

    /** Convenience: REC action with inline split delimiter (mirrors RTL {@code REC('/')}). */
    public static ActionSpec rec(String splitDelimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.REC, null, List.of(providers), null, splitDelimiter);
    }

    /** Convenience: REC action — all providers share the same cardinality and default traversal. */
    public static ActionSpec rec(int cardinality, ItemFilterConditionSpec... conditions) {
        var providers = new ArrayList<ProviderSpec>(conditions.length);
        for (var c : conditions) {
            providers.add(ProviderSpec.val(cardinality, c));
        }
        return new ActionSpec(OperationType.REC, null, List.copyOf(providers), null, null);
    }

    /** Convenience: AVP action with one provider. */
    public static ActionSpec avp(ProviderSpec provider) {
        return new ActionSpec(OperationType.AVP, null, List.of(provider), null, null);
    }

    /** Convenience: AVP action with a literal context-attribute provider. */
    public static ActionSpec avp(String literal) {
        return new ActionSpec(OperationType.AVP, null, List.of(ProviderSpec.ctxAttr(literal)), null, null);
    }

    /** Convenience: CONCAT action with given providers. */
    public static ActionSpec concat(ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null);
    }

    /** Convenience: FILL action with delimiter and providers. */
    public static ActionSpec fill(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.FILL, delimiter, List.of(providers), null, null);
    }

    /** Convenience: PREFIX action with delimiter and providers. */
    public static ActionSpec prefix(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.PREFIX, delimiter, List.of(providers), null, null);
    }

    /** Convenience: SUFFIX action with delimiter and providers. */
    public static ActionSpec suffix(String delimiter, ProviderSpec... providers) {
        return new ActionSpec(OperationType.SUFFIX, delimiter, List.of(providers), null, null);
    }
}
