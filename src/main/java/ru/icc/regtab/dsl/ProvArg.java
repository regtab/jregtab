package ru.icc.regtab.dsl;

/**
 * Argument of a DSL action factory ({@code rec(...)}, {@code avp(...)}, …):
 * either a cell-derived provider builder {@link Prov}, or a context literal
 * (RTL {@code 'text'} / {@code @'ATTR'='VALUE'}).
 */
public sealed interface ProvArg permits Prov, Rtl.Ctx, Rtl.CtxAvp {
}
