package ru.icc.regtab.tasks;

import java.util.Objects;

/**
 * Options for {@link RecordsetAssert#assertMatches(ru.icc.regtab.recordset.Recordset, ru.icc.regtab.recordset.Recordset, RecordsetMatchOptions)}.
 */
public final class RecordsetMatchOptions {

    public static final RecordsetMatchOptions DEFAULT_STRICT =
            new RecordsetMatchOptions(OrderPolicy.STRICT, OrderPolicy.STRICT, true);

    private final OrderPolicy attributeOrder;
    private final OrderPolicy recordOrder;
    private final boolean expectedHasHeader;

    public RecordsetMatchOptions(OrderPolicy attributeOrder, OrderPolicy recordOrder) {
        this(attributeOrder, recordOrder, true);
    }

    public RecordsetMatchOptions(OrderPolicy attributeOrder, OrderPolicy recordOrder, boolean expectedHasHeader) {
        this.attributeOrder = Objects.requireNonNull(attributeOrder, "attributeOrder");
        this.recordOrder = Objects.requireNonNull(recordOrder, "recordOrder");
        this.expectedHasHeader = expectedHasHeader;
    }

    public OrderPolicy attributeOrder() {
        return attributeOrder;
    }

    public OrderPolicy recordOrder() {
        return recordOrder;
    }

    public boolean expectedHasHeader() {
        return expectedHasHeader;
    }
}
