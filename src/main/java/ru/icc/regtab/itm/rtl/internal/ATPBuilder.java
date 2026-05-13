package ru.icc.regtab.itm.rtl.internal;

import ru.icc.regtab.itm.atp.spec.*;
import ru.icc.regtab.itm.rtl.RTLBaseVisitor;
import ru.icc.regtab.itm.rtl.RTLParser;
import ru.icc.regtab.itm.rtl.RtlCompileException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Walks an RTL parse tree and builds the ATP spec hierarchy. */
public final class ATPBuilder extends RTLBaseVisitor<Object> {

    private final Deque<List<ActionSpec>> inheritedActionsStack = new ArrayDeque<>();

    // -------- table --------

    @Override
    public TablePattern visitTablePattern(RTLParser.TablePatternContext ctx) {
        List<SubtablePattern> subtables = ctx.subtablePattern().stream()
                .map(sp -> (SubtablePattern) visit(sp))
                .toList();
        return new TablePattern(subtables);
    }

    // -------- subtable --------

    @Override
    public SubtablePattern visitSubtablePattern(RTLParser.SubtablePatternContext ctx) {
        if (ctx.implSubtablePattern() != null) return (SubtablePattern) visit(ctx.implSubtablePattern());
        return (SubtablePattern) visit(ctx.explSubtablePattern());
    }

    @Override
    public SubtablePattern visitImplSubtablePattern(RTLParser.ImplSubtablePatternContext ctx) {
        List<RowPattern> rows = ctx.rowPattern().stream()
                .map(r -> (RowPattern) visit(r))
                .toList();
        return new SubtablePattern(null, null, Quantifier.one(), rows);
    }

    @Override
    public SubtablePattern visitExplSubtablePattern(RTLParser.ExplSubtablePatternContext ctx) {
        Quantifier q = ctx.quantifier() != null ? buildQuantifier(ctx.quantifier()) : Quantifier.one();
        var body = ctx.subtablePatternBody();
        CellMatchCondition cond = body.cellMatchCond() != null
                ? buildCellMatchCondition(body.cellMatchCond()) : null;
        List<ActionSpec> local = body.actSpecs() != null ? buildActSpecs(body.actSpecs()) : List.of();
        pushInherited(local);
        try {
            List<RowPattern> rows = body.rowPattern().stream()
                    .map(r -> (RowPattern) visit(r))
                    .toList();
            return new SubtablePattern(null, cond, q, rows);
        } finally {
            inheritedActionsStack.pop();
        }
    }

    // -------- row --------

    @Override
    public RowPattern visitRowPattern(RTLParser.RowPatternContext ctx) {
        Quantifier q = ctx.quantifier() != null ? buildQuantifier(ctx.quantifier()) : Quantifier.one();
        var body = ctx.rowPatternBody();
        CellMatchCondition cond = body.cellMatchCond() != null
                ? buildCellMatchCondition(body.cellMatchCond()) : null;
        List<ActionSpec> local = body.actSpecs() != null ? buildActSpecs(body.actSpecs()) : List.of();
        pushInherited(local);
        try {
            List<SubrowPattern> subrows = body.subrowPattern().stream()
                    .map(sr -> (SubrowPattern) visit(sr))
                    .toList();
            return new RowPattern(null, cond, q, subrows);
        } finally {
            inheritedActionsStack.pop();
        }
    }

    // -------- subrow --------

    @Override
    public SubrowPattern visitSubrowPattern(RTLParser.SubrowPatternContext ctx) {
        if (ctx.implSubrowPattern() != null) return (SubrowPattern) visit(ctx.implSubrowPattern());
        return (SubrowPattern) visit(ctx.explSubrowPattern());
    }

    @Override
    public SubrowPattern visitImplSubrowPattern(RTLParser.ImplSubrowPatternContext ctx) {
        List<CellPattern> cells = ctx.cellPattern().stream()
                .map(cp -> (CellPattern) visit(cp))
                .toList();
        return new SubrowPattern(null, null, Quantifier.one(), cells);
    }

    @Override
    public SubrowPattern visitExplSubrowPattern(RTLParser.ExplSubrowPatternContext ctx) {
        Quantifier q = ctx.quantifier() != null ? buildQuantifier(ctx.quantifier()) : Quantifier.one();
        var body = ctx.subrowPatternBody();
        CellMatchCondition cond = body.cellMatchCond() != null
                ? buildCellMatchCondition(body.cellMatchCond()) : null;
        List<ActionSpec> local = body.actSpecs() != null ? buildActSpecs(body.actSpecs()) : List.of();
        pushInherited(local);
        try {
            List<CellPattern> cells = body.cellPattern().stream()
                    .map(cp -> (CellPattern) visit(cp))
                    .toList();
            return new SubrowPattern(null, cond, q, cells);
        } finally {
            inheritedActionsStack.pop();
        }
    }

    // -------- cell --------

    @Override
    public CellPattern visitCellPattern(RTLParser.CellPatternContext ctx) {
        Quantifier q = ctx.quantifier() != null ? buildQuantifier(ctx.quantifier()) : Quantifier.one();
        var body = ctx.cellPatternBody();
        CellMatchCondition cond = body.cellMatchCond() != null
                ? buildCellMatchCondition(body.cellMatchCond()) : null;

        // SKIP produces a cell pattern with null contentSpec
        if (isSkipAtom(body.contSpec())) {
            return new CellPattern(null, cond, q, null);
        }

        List<ActionSpec> local = body.actSpecs() != null ? buildActSpecs(body.actSpecs()) : List.of();
        pushInherited(local);
        try {
            ContentSpec cs = buildContentSpec(body.contSpec());
            return new CellPattern(null, cond, q, cs);
        } finally {
            inheritedActionsStack.pop();
        }
    }

    private static boolean isSkipAtom(RTLParser.ContSpecContext ctx) {
        return ctx.atomContSpec() != null
                && ctx.atomContSpec().itemDerivDir().SKIPPED() != null;
    }

    // -------- content spec --------

    private ContentSpec buildContentSpec(RTLParser.ContSpecContext ctx) {
        if (ctx.atomContSpec()  != null) return buildAtomicContentSpec(ctx.atomContSpec());
        if (ctx.delimContSpec() != null) return buildDelimitedContentSpec(ctx.delimContSpec());
        if (ctx.compContSpec()  != null) return buildCompoundContentSpec(ctx.compContSpec());
        if (ctx.condContSpec()  != null) return buildConditionalContentSpec(ctx.condContSpec());
        throw new RtlCompileException("Unknown content specification");
    }

    private AtomicContentSpec buildAtomicContentSpec(RTLParser.AtomContSpecContext ctx) {
        ItemDerivationDirective idd    = buildIdd(ctx.itemDerivDir());
        List<String>            tags   = ctx.tags()    != null ? buildTags(ctx.tags())                        : List.of();
        StringExtractor         extr   = ctx.strExtr() != null ? StringExtractorFactory.from(ctx.strExtr())   : null;
        List<ActionSpec>        local  = ctx.actSpecs() != null ? buildActSpecs(ctx.actSpecs())               : List.of();
        return new AtomicContentSpec(idd, extr, tags, mergeWithInherited(local));
    }

    private DelimitedContentSpec buildDelimitedContentSpec(RTLParser.DelimContSpecContext ctx) {
        AtomicContentSpec atom = buildAtomicContentSpec(ctx.atomContSpec());
        String delimiter = StringExtractorFactory.parseStringLiteral(ctx.separator().STRING().getText());
        return new DelimitedContentSpec(delimiter, atom);
    }

    private CompoundContentSpec buildCompoundContentSpec(RTLParser.CompContSpecContext ctx) {
        String openDelim = ctx.openDelim() != null
                ? StringExtractorFactory.parseStringLiteral(ctx.openDelim().STRING().getText()) : "";
        String closeDelim = ctx.closeDelim() != null
                ? StringExtractorFactory.parseStringLiteral(ctx.closeDelim().STRING().getText()) : "";

        var atomSpecs  = ctx.atomContSpec();
        var separators = ctx.separator();

        List<CompoundSegment> segments = new ArrayList<>();
        segments.add(new CompoundSegment(openDelim, buildAtomicContentSpec(atomSpecs.get(0))));
        for (int i = 0; i < separators.size(); i++) {
            String sep = StringExtractorFactory.parseStringLiteral(separators.get(i).STRING().getText());
            segments.add(new CompoundSegment(sep, buildAtomicContentSpec(atomSpecs.get(i + 1))));
        }
        return new CompoundContentSpec(segments, closeDelim);
    }

    private ConditionalContentSpec buildConditionalContentSpec(RTLParser.CondContSpecContext ctx) {
        CellMatchCondition cond = buildCellMatchCondition(ctx.cellMatchCond());
        var xSpecs = ctx.xContSpec();
        return new ConditionalContentSpec(cond, buildXContSpec(xSpecs.get(0)), buildXContSpec(xSpecs.get(1)));
    }

    private ContentSpec buildXContSpec(RTLParser.XContSpecContext ctx) {
        if (ctx.atomContSpec()  != null) return buildAtomicContentSpec(ctx.atomContSpec());
        if (ctx.delimContSpec() != null) return buildDelimitedContentSpec(ctx.delimContSpec());
        if (ctx.compContSpec()  != null) return buildCompoundContentSpec(ctx.compContSpec());
        throw new RtlCompileException("Unknown xContSpec alternative");
    }

    // -------- action specs --------

    private List<ActionSpec> buildActSpecs(RTLParser.ActSpecsContext ctx) {
        return ctx.actSpec().stream().map(this::buildActSpec).toList();
    }

    private ActionSpec buildActSpec(RTLParser.ActSpecContext ctx) {
        List<ProviderSpec> providers = buildProvSpecs(ctx.provSpecs());
        return buildOp(ctx.op(), providers);
    }

    private List<ProviderSpec> buildProvSpecs(RTLParser.ProvSpecsContext ctx) {
        return ctx.provSpec().stream().map(this::buildProvSpec).toList();
    }

    private ProviderSpec buildProvSpec(RTLParser.ProvSpecContext ctx) {
        if (ctx.tblProvSpec() != null) return ProviderTemplateResolver.resolve(ctx.tblProvSpec());
        String literal = StringExtractorFactory.parseStringLiteral(ctx.ctxProvSpec().STRING().getText());
        return ProviderSpec.ctxAttr(literal);
    }

    private static ActionSpec buildOp(RTLParser.OpContext ctx, List<ProviderSpec> providers) {
        if (ctx.AVP()    != null) return new ActionSpec(OperationType.AVP,    null, providers);
        if (ctx.REC()    != null) return new ActionSpec(OperationType.REC,    null, providers);
        if (ctx.CONCAT() != null) return new ActionSpec(OperationType.CONCAT, null, providers);
        if (ctx.fillOp() != null) {
            String d = ctx.fillOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.fillOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.FILL, d, providers);
        }
        if (ctx.prefixOp() != null) {
            String d = ctx.prefixOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.prefixOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.PREFIX, d, providers);
        }
        if (ctx.suffixOp() != null) {
            String d = ctx.suffixOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.suffixOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.SUFFIX, d, providers);
        }
        throw new RtlCompileException("Unknown operation type");
    }

    // -------- cell match condition --------

    private CellMatchCondition buildCellMatchCondition(RTLParser.CellMatchCondContext ctx) {
        var constr = ctx.cellMatchConstr();
        if (constr.regex() != null) return buildRegexCond(constr.regex());
        if (constr.blank() != null) return buildBlankCond(constr.blank());
        throw new RtlCompileException("Unknown cell match constraint");
    }

    private static CellMatchCondition buildRegexCond(RTLParser.RegexContext ctx) {
        String pattern = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        boolean neg = ctx.EXCLAMATION() != null;
        return neg
                ? new CellMatchCondition(c -> !c.text().matches(pattern))
                : new CellMatchCondition(c ->  c.text().matches(pattern));
    }

    private static CellMatchCondition buildBlankCond(RTLParser.BlankContext ctx) {
        boolean neg = ctx.EXCLAMATION() != null;
        return neg
                ? new CellMatchCondition(c -> !c.textBlank())
                : new CellMatchCondition(c ->  c.textBlank());
    }

    // -------- small helpers --------

    private static ItemDerivationDirective buildIdd(RTLParser.ItemDerivDirContext ctx) {
        if (ctx.VALUE()     != null) return ItemDerivationDirective.VAL;
        if (ctx.ATTRIBUTE() != null) return ItemDerivationDirective.ATTR;
        if (ctx.AUXILIARY() != null) return ItemDerivationDirective.AUX;
        if (ctx.SKIPPED()   != null) return ItemDerivationDirective.SKIP;
        throw new RtlCompileException("Unknown item derivation directive");
    }

    private static Quantifier buildQuantifier(RTLParser.QuantifierContext ctx) {
        if (ctx.zeroOrOne()  != null) return Quantifier.zeroOrOne();
        if (ctx.zeroOrMore() != null) return Quantifier.zeroOrMore();
        if (ctx.oneOrMore()  != null) return Quantifier.oneOrMore();
        if (ctx.exactly()    != null)
            return Quantifier.exactly(Integer.parseInt(ctx.exactly().INT().getText()));
        throw new RtlCompileException("Unknown quantifier");
    }

    private static List<String> buildTags(RTLParser.TagsContext ctx) {
        return ctx.TAG().stream().map(t -> t.getText()).toList();
    }

    // -------- inherited actions stack --------

    private List<ActionSpec> currentInherited() {
        return inheritedActionsStack.isEmpty() ? List.of() : inheritedActionsStack.peek();
    }

    private void pushInherited(List<ActionSpec> local) {
        if (local.isEmpty()) {
            inheritedActionsStack.push(currentInherited());
        } else {
            List<ActionSpec> merged = new ArrayList<>(currentInherited());
            merged.addAll(local);
            inheritedActionsStack.push(List.copyOf(merged));
        }
    }

    private List<ActionSpec> mergeWithInherited(List<ActionSpec> local) {
        List<ActionSpec> inherited = currentInherited();
        if (inherited.isEmpty()) return local;
        if (local.isEmpty())     return inherited;
        List<ActionSpec> all = new ArrayList<>(inherited);
        all.addAll(local);
        return List.copyOf(all);
    }
}
