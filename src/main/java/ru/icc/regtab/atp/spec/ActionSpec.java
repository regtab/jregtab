package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.operation.RecordKey;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Interpretation action specification S_act (def:action-spec):
 * S_act = (op, ⟨S_prov¹, …, S_provⁿ⟩).
 * <p>
 * A template from which a concrete {@link ru.icc.regtab.itm.semantics.action.InterpretationAction}
 * is constructed at match time, with the derived item bound as the anchor.
 * <p>
 * Inline REC parameters mirror RTL {@code REC(n)} / {@code REC('/')} syntax:
 * {@link #anchorPos} maps to {@link ru.icc.regtab.interpret.AnchorAttributeAtPosition},
 * {@link #splitDelimiter} maps to {@link ru.icc.regtab.interpret.DelimitedFieldSplit}.
 * {@link TablePattern#of(SubtablePattern...)} collects these automatically.
 *
 * @param operationType  working-state update operation op
 * @param delimiter      delimiter for FILL/PREFIX/SUFFIX (empty string if none); null for AVP/REC/CONCAT/JOIN
 * @param providers      sequence of item provider specifications ⟨S_prov¹, …, S_provⁿ⟩
 * @param anchorPos      inline anchor position for REC (null = none)
 * @param splitDelimiter inline split delimiter for REC (null = none)
 * @param key            key K for CONCAT/JOIN — key positions and/or key attribute names
 *                       ({@link RecordKey}); null treated as {@link RecordKey#EMPTY}
 * @param inherited      true if this action was inherited from a parent scope (row/subrow/subtable level),
 *                       false if explicitly specified on the cell's own contSpec
 */
public record ActionSpec(
        OperationType operationType,
        String delimiter,
        List<ProviderSpec> providers,
        Integer anchorPos,
        String splitDelimiter,
        RecordKey key,
        boolean inherited
) {
    /** Backward-compatible constructor: key=∅, inherited=false. */
    public ActionSpec(OperationType operationType, String delimiter,
                      List<ProviderSpec> providers, Integer anchorPos, String splitDelimiter) {
        this(operationType, delimiter, providers, anchorPos, splitDelimiter, RecordKey.EMPTY, false);
    }

    /** Backward-compatible constructor: the key given as key positions only. */
    public ActionSpec(OperationType operationType, String delimiter,
                      List<ProviderSpec> providers, Integer anchorPos, String splitDelimiter,
                      Set<Integer> keyPositions, boolean inherited) {
        this(operationType, delimiter, providers, anchorPos, splitDelimiter,
             RecordKey.positions(keyPositions != null ? keyPositions : Set.of()), inherited);
    }

    public ActionSpec {
        Objects.requireNonNull(operationType, "operationType");
        providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        key = key != null ? key : RecordKey.EMPTY;
        for (var p : providers) {
            if (p.isContextLiteral()) {
                var ctxType = p.contextLiteral().type();
                boolean isConstAvp = p.contextLiteral().constValue() != null;
                if (operationType == OperationType.JOIN || operationType == OperationType.CONCAT)
                    throw new IllegalArgumentException(
                            operationType + " action does not allow context literals");
                if (operationType == OperationType.REC && !isConstAvp && ctxType != ItemType.VALUE)
                    throw new IllegalArgumentException(
                            "REC action requires a VALUE context literal, got " + ctxType);
                if (operationType == OperationType.AVP && ctxType != ItemType.ATTRIBUTE)
                    throw new IllegalArgumentException(
                            "AVP action requires an ATTRIBUTE context literal, got " + ctxType);
            } else {
                var kind = p.targetItemKind();
                if ((operationType == OperationType.REC || operationType == OperationType.CONCAT
                        || operationType == OperationType.JOIN)
                        && kind != CellDerivedProviderKind.VAL)
                    throw new IllegalArgumentException(
                            operationType + " action requires a VAL provider, got " + kind);
                if (operationType == OperationType.AVP && kind != CellDerivedProviderKind.ATTR)
                    throw new IllegalArgumentException(
                            "AVP action requires an ATTR provider, got " + kind);
            }
        }
    }

    /** Returns a copy with {@code inherited=true}; returns {@code this} if already inherited. */
    public ActionSpec asInherited() {
        return inherited ? this
                : new ActionSpec(operationType, delimiter, providers, anchorPos, splitDelimiter,
                                 key, true);
    }

    /** The key positions of {@link #key()} (K ∩ ℕ₀); kept for backward compatibility. */
    public Set<Integer> keyPositions() {
        return key.positions();
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

    /** Convenience: CONCAT action with K=∅ — fold the provided records into the anchor's record. */
    public static ActionSpec concat(ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null, RecordKey.EMPTY, false);
    }

    /** Convenience: CONCAT^K action with the key K given as positions and/or attribute names (mirrors RTL {@code CONCAT(0, 'A')}). */
    public static ActionSpec concat(RecordKey key, ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null, key, false);
    }

    /** Convenience: CONCAT^K action with a single key attribute name (mirrors RTL {@code CONCAT('A')}). */
    public static ActionSpec concat(String keyName, ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null, RecordKey.names(keyName), false);
    }

    /** Convenience: CONCAT^K action with explicit key positions as a Set (mirrors RTL {@code CONCAT(k1,k2,...)}). */
    public static ActionSpec concat(Set<Integer> keyPositions, ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null, keyPositions, false);
    }

    /** Convenience: CONCAT^K action with a single key position (mirrors RTL {@code CONCAT(k)}). */
    public static ActionSpec concat(int keyPosition, ProviderSpec... providers) {
        return new ActionSpec(OperationType.CONCAT, null, List.of(providers), null, null, Set.of(keyPosition), false);
    }

    /** Convenience: JOIN action with K=∅ — the record product (cross product with the provided records). */
    public static ActionSpec join(ProviderSpec... providers) {
        return new ActionSpec(OperationType.JOIN, null, List.of(providers), null, null, RecordKey.EMPTY, false);
    }

    /** Convenience: JOIN^K action with the key K given as positions and/or attribute names (mirrors RTL {@code JOIN(0, 'A')}). */
    public static ActionSpec join(RecordKey key, ProviderSpec... providers) {
        return new ActionSpec(OperationType.JOIN, null, List.of(providers), null, null, key, false);
    }

    /** Convenience: JOIN^K action with a single key attribute name (mirrors RTL {@code JOIN('A')}). */
    public static ActionSpec join(String keyName, ProviderSpec... providers) {
        return new ActionSpec(OperationType.JOIN, null, List.of(providers), null, null, RecordKey.names(keyName), false);
    }

    /** Convenience: JOIN^K action — equi-join on the key positions K (mirrors RTL {@code JOIN(k1,k2,...)}). */
    public static ActionSpec join(Set<Integer> keyPositions, ProviderSpec... providers) {
        return new ActionSpec(OperationType.JOIN, null, List.of(providers), null, null, keyPositions, false);
    }

    /** Convenience: JOIN^K action with a single key position (mirrors RTL {@code JOIN(k)}). */
    public static ActionSpec join(int keyPosition, ProviderSpec... providers) {
        return new ActionSpec(OperationType.JOIN, null, List.of(providers), null, null, Set.of(keyPosition), false);
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
