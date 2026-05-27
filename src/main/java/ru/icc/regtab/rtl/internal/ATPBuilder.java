package ru.icc.regtab.rtl.internal;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.interpret.DelimitedFieldSplit;
import ru.icc.regtab.interpret.RecordsetTransformation;
import ru.icc.regtab.interpret.WhitespaceNormalization;
import ru.icc.regtab.rtl.RTLBaseVisitor;
import ru.icc.regtab.rtl.RTLParser;
import ru.icc.regtab.rtl.RtlCompileException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Walks an RTL parse tree and builds the ATP spec hierarchy. */
public final class ATPBuilder extends RTLBaseVisitor<Object> {

    private final Deque<List<ActionSpec>> inheritedActionsStack = new ArrayDeque<>();

    // -------- table --------

    @Override
    public TablePattern visitTablePattern(RTLParser.TablePatternContext ctx) {
        List<SubtablePattern> subtables = ctx.subtablePattern().stream()
                .map(sp -> (SubtablePattern) visit(sp))
                .toList();
        List<RecordsetTransformation> transformations = ctx.settings() != null
                ? buildTransformations(ctx.settings())
                : List.of();
        return new TablePattern(subtables, transformations);
    }

    private static List<RecordsetTransformation> buildTransformations(RTLParser.SettingsContext ctx) {
        return ctx.setting().stream()
                .map(ATPBuilder::buildTransformation)
                .toList();
    }

    private static RecordsetTransformation buildTransformation(RTLParser.SettingContext ctx) {
        if (ctx.normSetting()  != null) return new WhitespaceNormalization();
        if (ctx.anchSetting()  != null)
            return new AnchorAttributeAtPosition(Integer.parseInt(ctx.anchSetting().INT().getText()));
        if (ctx.splitSetting() != null) {
            String delim = StringExtractorFactory.parseStringLiteral(ctx.splitSetting().STRING().getText());
            return new DelimitedFieldSplit(delim);
        }
        throw new RtlCompileException("Unknown setting type");
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
        return new SubtablePattern(null, Quantifier.one(), rows);
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
            return new SubtablePattern(cond, q, rows);
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
            return new RowPattern(cond, q, subrows);
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
        return new SubrowPattern(null, Quantifier.one(), cells);
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
            return new SubrowPattern(cond, q, cells);
        } finally {
            inheritedActionsStack.pop();
        }
    }

    // -------- cell --------

    @Override
    public CellPattern visitCellPattern(RTLParser.CellPatternContext ctx) {
        Quantifier q = ctx.quantifier() != null ? buildQuantifier(ctx.quantifier()) : Quantifier.one();
        var body = ctx.cellPatternBody();
        // [] and [BLANK?] (no contSpec) are shorthand for [SKIP]
        if (body == null || body.contSpec() == null || isSkipAtom(body.contSpec())) {
            CellMatchCondition cond = body != null && body.cellMatchCond() != null
                    ? buildCellMatchCondition(body.cellMatchCond()) : null;
            return new CellPattern(cond, q, null);
        }
        CellMatchCondition cond = body.cellMatchCond() != null
                ? buildCellMatchCondition(body.cellMatchCond()) : null;

        List<ActionSpec> local = body.actSpecs() != null ? buildActSpecs(body.actSpecs()) : List.of();
        pushInherited(local);
        try {
            ContentSpec cs = buildContentSpec(body.contSpec());
            return new CellPattern(cond, q, cs);
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
        ItemDerivationDirective idd  = buildIdd(ctx.itemDerivDir());
        List<String>            tags = ctx.tags()    != null ? buildTags(ctx.tags())                      : List.of();
        StringExtractor         extr = ctx.strExtr() != null ? StringExtractorFactory.from(ctx.strExtr()) : null;
        List<ActionSpec>        local = ctx.actSpecs() != null ? buildActSpecs(ctx.actSpecs(), idd)        : List.of();
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

        var segs       = ctx.compSeg();
        var separators = ctx.separator();

        List<CompoundSegment> segments = new ArrayList<>();
        segments.add(new CompoundSegment(openDelim, buildCompSeg(segs.get(0))));
        for (int i = 0; i < separators.size(); i++) {
            String sep = StringExtractorFactory.parseStringLiteral(separators.get(i).STRING().getText());
            segments.add(new CompoundSegment(sep, buildCompSeg(segs.get(i + 1))));
        }
        return new CompoundContentSpec(segments, closeDelim);
    }

    private ContentSpec buildCompSeg(RTLParser.CompSegContext ctx) {
        if (ctx.atomContSpec() != null) return buildAtomicContentSpec(ctx.atomContSpec());
        return buildDelimitedContentSpec(ctx.delimContSpec());
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

    /** Used for inherited actSpecs (subtable/row/subrow/cell level) — kind inferred from op only. */
    private List<ActionSpec> buildActSpecs(RTLParser.ActSpecsContext ctx) {
        return ctx.actSpec().stream().map(this::buildActSpec).toList();
    }

    private ActionSpec buildActSpec(RTLParser.ActSpecContext ctx) {
        List<ProviderSpec> providers = buildProvSpecs(ctx.provSpecs(), ctx.op(), null);
        return buildOp(ctx.op(), providers);
    }

    /** Used for actSpecs directly on an atomContSpec — kind inferred from op + anchor type. */
    private List<ActionSpec> buildActSpecs(RTLParser.ActSpecsContext ctx, ItemDerivationDirective anchorType) {
        return ctx.actSpec().stream().map(as -> buildActSpec(as, anchorType)).toList();
    }

    private ActionSpec buildActSpec(RTLParser.ActSpecContext ctx, ItemDerivationDirective anchorType) {
        List<ProviderSpec> providers = buildProvSpecs(ctx.provSpecs(), ctx.op(), anchorType);
        return buildOp(ctx.op(), providers);
    }

    private List<ProviderSpec> buildProvSpecs(RTLParser.ProvSpecsContext ctx,
                                              RTLParser.OpContext op,
                                              ItemDerivationDirective anchorType) {
        return ctx.provSpec().stream().map(ps -> buildProvSpec(ps, op, anchorType)).toList();
    }

    private ProviderSpec buildProvSpec(RTLParser.ProvSpecContext ctx,
                                       RTLParser.OpContext op,
                                       ItemDerivationDirective anchorType) {
        if (ctx.tblProvSpec() != null)
            return ProviderTemplateResolver.resolve(ctx.tblProvSpec(), op, anchorType);
        if (ctx.ctxAvpSpec() != null) {
            String name  = StringExtractorFactory.parseStringLiteral(ctx.ctxAvpSpec().STRING(0).getText());
            String value = StringExtractorFactory.parseStringLiteral(ctx.ctxAvpSpec().STRING(1).getText());
            return ProviderSpec.ctxAvp(name, value);
        }
        String literal = StringExtractorFactory.parseStringLiteral(ctx.ctxProvSpec().STRING().getText());
        if (op != null && (op.recOp() != null || op.joinOp() != null))
            return ProviderSpec.ctxVal(literal);
        return ProviderSpec.ctxAttr(literal);
    }

    private static ActionSpec buildOp(RTLParser.OpContext ctx, List<ProviderSpec> providers) {
        if (ctx.AVP()    != null) return new ActionSpec(OperationType.AVP,    null, providers, null, null);
        if (ctx.recOp()  != null) {
            RTLParser.RecOpContext rec = ctx.recOp();
            Integer anchorPos      = rec.INT()    != null ? Integer.parseInt(rec.INT().getText()) : null;
            String  splitDelimiter = rec.STRING() != null ? StringExtractorFactory.parseStringLiteral(rec.STRING().getText()) : null;
            return new ActionSpec(OperationType.REC, null, providers, anchorPos, splitDelimiter);
        }
        if (ctx.joinOp() != null) {
            Set<Integer> kp = new LinkedHashSet<>();
            for (var t : ctx.joinOp().INT()) kp.add(Integer.parseInt(t.getText()));
            return new ActionSpec(OperationType.JOIN, null, providers, null, null, Set.copyOf(kp), false);
        }
        if (ctx.fillOp() != null) {
            String d = ctx.fillOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.fillOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.FILL, d, providers, null, null);
        }
        if (ctx.prefixOp() != null) {
            String d = ctx.prefixOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.prefixOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.PREFIX, d, providers, null, null);
        }
        if (ctx.suffixOp() != null) {
            String d = ctx.suffixOp().STRING() != null
                    ? StringExtractorFactory.parseStringLiteral(ctx.suffixOp().STRING().getText()) : "";
            return new ActionSpec(OperationType.SUFFIX, d, providers, null, null);
        }
        throw new RtlCompileException("Unknown operation type");
    }

    // -------- cell match condition --------

    private static CellMatchCondition buildCellMatchCondition(RTLParser.CellMatchCondContext ctx) {
        var constr = ctx.cellMatchConstr();
        if (constr.regex()    != null) return buildRegexCond(constr.regex());
        if (constr.blank()    != null) return buildBlankCond(constr.blank());
        if (constr.contains() != null) return buildContainsCond(constr.contains());
        throw new RtlCompileException("Unknown cell match constraint");
    }

    private static CellMatchCondition buildContainsCond(RTLParser.ContainsContext ctx) {
        String substring = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        return ctx.EXCLAMATION() != null
                ? new CellMatchCondition(new CellPredicate.NotContains(substring))
                : new CellMatchCondition(new CellPredicate.Contains(substring));
    }

    private static CellMatchCondition buildRegexCond(RTLParser.RegexContext ctx) {
        String pattern = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        return ctx.EXCLAMATION() != null
                ? new CellMatchCondition(new CellPredicate.NotRegexMatched(pattern))
                : new CellMatchCondition(new CellPredicate.RegexMatched(pattern));
    }

    private static CellMatchCondition buildBlankCond(RTLParser.BlankContext ctx) {
        return ctx.EXCLAMATION() != null
                ? new CellMatchCondition(CellPredicate.NotBlank.INSTANCE)
                : new CellMatchCondition(CellPredicate.Blank.INSTANCE);
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
        return ctx.tagItem().stream()
                .map(t -> "#" + StringExtractorFactory.parseStringLiteral(t.STRING().getText()))
                .toList();
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
        List<ActionSpec> fromStack = currentInherited();
        if (fromStack.isEmpty()) return local;
        // Everything from the stack is inherited at this cell level — mark it
        List<ActionSpec> markedInherited = fromStack.stream()
                .map(ActionSpec::asInherited)
                .toList();
        if (local.isEmpty()) return markedInherited;
        List<ActionSpec> all = new ArrayList<>(markedInherited);
        all.addAll(local);
        return List.copyOf(all);
    }
}
