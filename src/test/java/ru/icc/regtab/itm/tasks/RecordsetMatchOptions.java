package ru.icc.regtab.itm.tasks;

import java.util.Objects;

/**
 * Options for {@link RecordsetAssert#assertMatches(ru.icc.regtab.itm.recordset.Recordset, ru.icc.regtab.itm.recordset.Recordset, RecordsetMatchOptions)}.
 */
public final class RecordsetMatchOptions {

    public static final RecordsetMatchOptions DEFAULT_STRICT =
            new RecordsetMatchOptions(OrderPolicy.STRICT, OrderPolicy.STRICT);

    private final OrderPolicy attributeOrder;
    private final OrderPolicy recordOrder;

    public RecordsetMatchOptions(OrderPolicy attributeOrder, OrderPolicy recordOrder) {
        this.attributeOrder = Objects.requireNonNull(attributeOrder, "attributeOrder");
        this.recordOrder = Objects.requireNonNull(recordOrder, "recordOrder");
    }

    public OrderPolicy attributeOrder() {
        return attributeOrder;
    }

    public OrderPolicy recordOrder() {
        return recordOrder;
    }
}
