package ru.icc.regtab.dsl;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemDerivationDirective;
import ru.icc.regtab.atp.spec.OperationType;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.syntax.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Embedded RTL — a Java DSL mirroring RTL syntax, building the same ATP objects
 * as {@link ru.icc.regtab.rtl.RtlCompiler}.
 *
 * <p>Import statically and read as RTL:
 * <pre>{@code
 * import static ru.icc.regtab.dsl.Rtl.*;
 *
 * // RTL:  { [ [VAL : ST*->REC] [VAL]{2} []+ ]
 * //         [ []               [VAL]{4} []+ ] }+
 * TablePattern p = table(
 *     sub( row( cell(VAL, rec(ST.unbounded())), cell(VAL).exactly(2), skip().oneOrMore() ),
 *          row( skip(),                         cell(VAL).exactly(4), skip().oneOrMore() )
 *     ).oneOrMore());
 * }</pre>
 *
 * <p>Escape hatches into Java (no RTL analog): {@code cell(where("desc", c -> …), VAL)}
 * for cell match conditions and {@code ST.where("desc", (a, c) -> …)} for provider
 * constraints.
 *
 * <p><b>DRAFT (design spike B1)</b> — vocabulary is subject to review; see
 * {@code plans/RTL_EMBEDDED_DSL.md}.
 */
public final class Rtl {

    private Rtl() {}

    // ==== item derivation directives (RTL: VAL / ATTR / AUX / _) ====

    public static final ItemDerivationDirective VAL  = ItemDerivationDirective.VAL;
    public static final ItemDerivationDirective ATTR = ItemDerivationDirective.ATTR;
    public static final ItemDerivationDirective AUX  = ItemDerivationDirective.AUX;
    public static final ItemDerivationDirective SKIP = ItemDerivationDirective.SKIP;

    // ==== string extractors (RTL: = NORM, = TRIM, = UC, = LC) ====

    public static final StringExtractor NORM = StringExtractor.WhitespaceNormalized.INSTANCE;
    public static final StringExtractor TRIM = StringExtractor.Trimmed.INSTANCE;
    public static final StringExtractor UC   = StringExtractor.UpperCase.INSTANCE;
    public static final StringExtractor LC   = StringExtractor.LowerCase.INSTANCE;

    /** RTL {@code REPL("regex","replacement")}. */
    public static StringExtractor repl(String regex, String replacement) {
        return new StringExtractor.Replaced(regex, replacement);
    }

    /** RTL {@code SUBSTR(begin,length)} — here as begin/end offsets. */
    public static StringExtractor substr(int begin, int end) {
        return new StringExtractor.Substring(begin, end);
    }

    /** RTL extractor chain {@code =REPL(…).TRIM}. */
    public static StringExtractor chain(StringExtractor... steps) {
        return new StringExtractor.Chain(List.of(steps));
    }

    // ==== recordset transformations (RTL settings prefix <NORM,ANCH(n),SPLIT("s")>) ====

    /** RTL setting {@code NORM} — use with {@code table(…).withTransformations(norm(), …)}. */
    public static ru.icc.regtab.interpret.RecordsetTransformation norm() {
        return new ru.icc.regtab.interpret.WhitespaceNormalization();
    }

    /** RTL setting {@code ANCH(n)}. */
    public static ru.icc.regtab.interpret.RecordsetTransformation anch(int position) {
        return new ru.icc.regtab.interpret.AnchorAttributeAtPosition(position);
    }

    /** RTL setting {@code SPLIT("s")}. */
    public static ru.icc.regtab.interpret.RecordsetTransformation split(String delimiter) {
        return new ru.icc.regtab.interpret.DelimitedFieldSplit(delimiter);
    }

    // ==== inherited action specs (RTL: actSpecs at table/subtable/row/subrow/cell level) ====

    /**
     * Level-scoped action specs (RTL {@code acts} written before the nested patterns).
     * They are merged down into every atom of the enclosed patterns with
     * {@code inherited=true}, exactly as the RTL compiler does:
     * {@code row(acts(rec(BW.unbounded())), subrow(…))} ≙ RTL {@code [ BW*->REC { … } ]}.
     */
    public static Acts acts(ActionSpec... actions) {
        return new Acts(List.of(actions));
    }

    /** Wrapper for level-scoped action specs — see {@link #acts(ActionSpec...)}. */
    public record Acts(List<ActionSpec> actions) {
        List<ActionSpec> marked() {
            return actions.stream().map(ActionSpec::asInherited).toList();
        }
    }

    // ==== pattern levels ====

    /** Table pattern; inline REC(n)/REC('s') params become transformations automatically. */
    public static TablePattern table(SubtablePattern... subtables) {
        return TablePattern.of(subtables);
    }

    /** Table pattern with table-level inherited actions (RTL {@code acts subtables}). */
    public static TablePattern table(Acts acts, SubtablePattern... subtables) {
        return TablePattern.of(mergeDown(List.of(subtables), acts.marked())
                .toArray(SubtablePattern[]::new));
    }

    /** Table pattern with a table-level condition (RTL {@code cond ? subtables}). */
    public static TablePattern table(CellPredicate condition, SubtablePattern... subtables) {
        TablePattern base = TablePattern.of(subtables);
        return new TablePattern(new CellMatchCondition(condition),
                base.subtablePatterns(), base.transformations());
    }

    /** Table pattern with a table-level condition and inherited actions. */
    public static TablePattern table(CellPredicate condition, Acts acts, SubtablePattern... subtables) {
        TablePattern base = table(acts, subtables);
        return new TablePattern(new CellMatchCondition(condition),
                base.subtablePatterns(), base.transformations());
    }

    /** Subtable pattern (RTL {@code { rows }}); quantify with postfix methods. */
    public static SubtablePattern sub(RowPattern... rows) {
        return new SubtablePattern(null, Quantifier.one(), List.of(rows));
    }

    /** Subtable pattern with subtable-level inherited actions. */
    public static SubtablePattern sub(Acts acts, RowPattern... rows) {
        return new SubtablePattern(null, Quantifier.one(), mergeDownRows(List.of(rows), acts.marked()));
    }

    /** Subtable pattern with a subtable-level condition (RTL {@code { cond ? rows }}). */
    public static SubtablePattern sub(CellPredicate condition, RowPattern... rows) {
        return new SubtablePattern(new CellMatchCondition(condition), Quantifier.one(), List.of(rows));
    }

    /** Subtable pattern with a subtable-level condition and inherited actions. */
    public static SubtablePattern sub(CellPredicate condition, Acts acts, RowPattern... rows) {
        return new SubtablePattern(new CellMatchCondition(condition), Quantifier.one(),
                mergeDownRows(List.of(rows), acts.marked()));
    }

    /** Row pattern with one implicit subrow (RTL {@code [ cells ]}). */
    public static RowPattern row(CellPattern... cells) {
        return new RowPattern(null, Quantifier.one(),
                List.of(new SubrowPattern(null, Quantifier.one(), List.of(cells))));
    }

    /** Row pattern with explicit subrow patterns (RTL {@code [ { cells } … ]}). */
    public static RowPattern row(SubrowPattern... subrows) {
        return new RowPattern(null, Quantifier.one(), List.of(subrows));
    }

    /** Row pattern with row-level inherited actions (RTL {@code [ acts cells ]}). */
    public static RowPattern row(Acts acts, CellPattern... cells) {
        return new RowPattern(null, Quantifier.one(),
                List.of(new SubrowPattern(null, Quantifier.one(),
                        mergeDownCells(List.of(cells), acts.marked()))));
    }

    /** Row pattern with row-level inherited actions and explicit subrows. */
    public static RowPattern row(Acts acts, SubrowPattern... subrows) {
        return new RowPattern(null, Quantifier.one(), mergeDownSubrows(List.of(subrows), acts.marked()));
    }

    /** Row pattern with a row-level condition (RTL {@code [ cond ? cells ]}). */
    public static RowPattern row(CellPredicate condition, CellPattern... cells) {
        return new RowPattern(new CellMatchCondition(condition), Quantifier.one(),
                List.of(new SubrowPattern(null, Quantifier.one(), List.of(cells))));
    }

    /** Row pattern with a row-level condition and inherited actions. */
    public static RowPattern row(CellPredicate condition, Acts acts, CellPattern... cells) {
        return new RowPattern(new CellMatchCondition(condition), Quantifier.one(),
                List.of(new SubrowPattern(null, Quantifier.one(),
                        mergeDownCells(List.of(cells), acts.marked()))));
    }

    /** Explicit subrow pattern (RTL {@code { cells }} inside a row). */
    public static SubrowPattern subrow(CellPattern... cells) {
        return new SubrowPattern(null, Quantifier.one(), List.of(cells));
    }

    /** Explicit subrow pattern with subrow-level inherited actions. */
    public static SubrowPattern subrow(Acts acts, CellPattern... cells) {
        return new SubrowPattern(null, Quantifier.one(), mergeDownCells(List.of(cells), acts.marked()));
    }

    /** Explicit subrow pattern with a subrow-level condition (RTL {@code { cond ? cells }}). */
    public static SubrowPattern subrow(CellPredicate condition, CellPattern... cells) {
        return new SubrowPattern(new CellMatchCondition(condition), Quantifier.one(), List.of(cells));
    }

    // ==== merge-down of inherited actions (mirrors ATPBuilder's inheritance stack) ====

    private static List<SubtablePattern> mergeDown(List<SubtablePattern> subs, List<ActionSpec> inh) {
        return subs.stream()
                .map(s -> new SubtablePattern(s.condition(), s.quantifier(),
                        mergeDownRows(s.rowPatterns(), inh)))
                .toList();
    }

    private static List<RowPattern> mergeDownRows(List<RowPattern> rows, List<ActionSpec> inh) {
        return rows.stream()
                .map(r -> new RowPattern(r.condition(), r.quantifier(),
                        mergeDownSubrows(r.subrowPatterns(), inh)))
                .toList();
    }

    private static List<SubrowPattern> mergeDownSubrows(List<SubrowPattern> subrows, List<ActionSpec> inh) {
        return subrows.stream()
                .map(sr -> new SubrowPattern(sr.condition(), sr.quantifier(),
                        mergeDownCells(sr.cellPatterns(), inh)))
                .toList();
    }

    private static List<CellPattern> mergeDownCells(List<CellPattern> cells, List<ActionSpec> inh) {
        return cells.stream()
                .map(c -> c.contentSpec() == null ? c
                        : new CellPattern(c.condition(), c.quantifier(), mergeDown(c.contentSpec(), inh)))
                .toList();
    }

    private static ContentSpec mergeDown(ContentSpec cs, List<ActionSpec> inh) {
        return switch (cs) {
            case AtomicContentSpec a -> {
                var merged = new ArrayList<>(inh);
                merged.addAll(a.actions());
                yield new AtomicContentSpec(a.idd(), a.extractor(), a.tags(), merged);
            }
            case ru.icc.regtab.atp.spec.DelimitedContentSpec d ->
                    new ru.icc.regtab.atp.spec.DelimitedContentSpec(d.delimiter(),
                            (AtomicContentSpec) mergeDown(d.atomicSpec(), inh));
            case ru.icc.regtab.atp.spec.CompoundContentSpec c ->
                    new ru.icc.regtab.atp.spec.CompoundContentSpec(
                            c.segments().stream()
                                    .map(s -> new ru.icc.regtab.atp.spec.CompoundSegment(
                                            s.leadingDelimiter(), mergeDown(s.spec(), inh)))
                                    .toList(),
                            c.trailingDelimiter());
            case ConditionalContentSpec x -> new ConditionalContentSpec(x.condition(),
                    mergeDown(x.positive(), inh), mergeDown(x.negative(), inh));
        };
    }

    // ==== cells ====

    /** Skip cell (RTL {@code []}). */
    public static CellPattern skip() {
        return CellPattern.skip();
    }

    /** Cell with an atomic content spec (RTL {@code [VAL : actions]}). */
    public static CellPattern cell(ItemDerivationDirective idd, ActionSpec... actions) {
        return new CellPattern(null, Quantifier.one(), atom(idd, actions));
    }

    /** Cell with an arbitrary content spec (compound, delimited, conditional, tagged atom …). */
    public static CellPattern cell(ContentSpec cs) {
        return new CellPattern(null, Quantifier.one(), cs);
    }

    /** Condition-only cell (RTL {@code [BLANK]}) — consumes a matching cell, produces no item. */
    public static CellPattern cell(CellPredicate condition) {
        return new CellPattern(new CellMatchCondition(condition), Quantifier.one(), null);
    }

    /** Guarded cell (RTL {@code [cond ? VAL : actions]}). */
    public static CellPattern cell(CellPredicate condition, ItemDerivationDirective idd, ActionSpec... actions) {
        return new CellPattern(new CellMatchCondition(condition), Quantifier.one(), atom(idd, actions));
    }

    /** Guarded cell with an arbitrary content spec (RTL {@code [cond ? S_cont]}). */
    public static CellPattern cell(CellPredicate condition, ContentSpec cs) {
        return new CellPattern(new CellMatchCondition(condition), Quantifier.one(), cs);
    }

    /** Cell with cell-level inherited actions before the content spec (RTL {@code [acts S_cont]}). */
    public static CellPattern cell(Acts acts, ContentSpec cs) {
        return new CellPattern(null, Quantifier.one(), mergeDown(cs, acts.marked()));
    }

    /** Guarded cell with cell-level inherited actions (RTL {@code [cond ? acts S_cont]}). */
    public static CellPattern cell(CellPredicate condition, Acts acts, ContentSpec cs) {
        return new CellPattern(new CellMatchCondition(condition), Quantifier.one(),
                mergeDown(cs, acts.marked()));
    }

    // ==== atomic content specs (for compound/delimited/conditional/tagged forms) ====

    /** Atomic VAL (RTL {@code VAL : actions}); chain {@code .tagged(…)}, {@code .extract(…)}, {@code .splitBy(…)}, {@code .then(…)}. */
    public static AtomicContentSpec val(ActionSpec... actions) {
        return atom(ItemDerivationDirective.VAL, actions);
    }

    /** Atomic ATTR. */
    public static AtomicContentSpec attr(ActionSpec... actions) {
        return atom(ItemDerivationDirective.ATTR, actions);
    }

    /** Atomic AUX. */
    public static AtomicContentSpec aux(ActionSpec... actions) {
        return atom(ItemDerivationDirective.AUX, actions);
    }

    private static AtomicContentSpec atom(ItemDerivationDirective idd, ActionSpec... actions) {
        return new AtomicContentSpec(idd, null, List.of(), List.of(actions));
    }

    // ==== conditional content spec (RTL: cond ? S⁺ | S⁻) ====

    /** Conditional spec (RTL {@code cond ? S⁺ | S⁻}). */
    public static ConditionalContentSpec when(CellPredicate condition, ContentSpec positive, ContentSpec negative) {
        return new ConditionalContentSpec(new CellMatchCondition(condition), positive, negative);
    }

    /** Conditional spec over bare directives (RTL {@code BLANK ? _ | VAL}). */
    public static ConditionalContentSpec when(CellPredicate condition,
                                              ItemDerivationDirective positive,
                                              ItemDerivationDirective negative) {
        return when(condition, atom(positive), atom(negative));
    }

    /** Conditional spec, directive-positive form (RTL {@code BLANK ? _ | VAL : acts}). */
    public static ConditionalContentSpec when(CellPredicate condition,
                                              ItemDerivationDirective positive,
                                              ContentSpec negative) {
        return when(condition, atom(positive), negative);
    }

    /** Conditional spec, directive-negative form (RTL {@code BLANK ? VAL : acts | _}). */
    public static ConditionalContentSpec when(CellPredicate condition,
                                              ContentSpec positive,
                                              ItemDerivationDirective negative) {
        return when(condition, positive, atom(negative));
    }

    // ==== cell match predicates (RTL: BLANK, "regex", ~"sub" + negations) ====

    /** RTL {@code BLANK}. */
    public static CellPredicate blank() { return CellPredicate.Blank.INSTANCE; }

    /** RTL {@code !BLANK}. */
    public static CellPredicate notBlank() { return CellPredicate.NotBlank.INSTANCE; }

    /** RTL {@code "regex"}. */
    public static CellPredicate re(String regex) { return new CellPredicate.RegexMatched(regex); }

    /** RTL {@code !"regex"}. */
    public static CellPredicate notRe(String regex) { return new CellPredicate.NotRegexMatched(regex); }

    /** RTL {@code ~"sub"}. */
    public static CellPredicate contains(String substring) { return new CellPredicate.Contains(substring); }

    /** RTL {@code !~"sub"}. */
    public static CellPredicate notContains(String substring) { return new CellPredicate.NotContains(substring); }

    /** Escape hatch: arbitrary Java cell predicate (no RTL analog). */
    public static CellPredicate where(String description, Predicate<Cell> predicate) {
        return new CellPredicate.Custom(description, predicate);
    }

    // ==== providers: named spatial/content constants (RTL keywords, 1:1) ====

    public static final Prov LT  = new Prov(FilterTerm.LeftOf.INSTANCE);
    public static final Prov RT  = new Prov(FilterTerm.RightOf.INSTANCE);
    public static final Prov AV  = new Prov(FilterTerm.Above.INSTANCE);
    public static final Prov BW  = new Prov(FilterTerm.Below.INSTANCE);
    public static final Prov ROW = new Prov(FilterTerm.SameRow.INSTANCE);
    public static final Prov COL = new Prov(FilterTerm.SameCol.INSTANCE);
    public static final Prov SR  = new Prov(FilterTerm.SameSubrow.INSTANCE);
    public static final Prov SC  = new Prov(FilterTerm.SameSubcol.INSTANCE);
    public static final Prov ST  = new Prov(FilterTerm.SameSubtable.INSTANCE);
    public static final Prov NCL = new Prov(FilterTerm.NotSameCell.INSTANCE);
    public static final Prov CL  = new Prov(FilterTerm.SameCell.INSTANCE);
    public static final Prov STR = new Prov(FilterTerm.SameStr.INSTANCE);

    // ==== providers: positional constraints (RTL: Cn, Ca..b, C+n, …) ====

    /** RTL {@code Cn} — absolute column. */
    public static Prov C(int n) { return new Prov(new FilterTerm.ColExact(n)); }

    /** RTL {@code Ca..b} — absolute column range. */
    public static Prov C(int lo, int hi) { return new Prov(new FilterTerm.ColAbsoluteRange(lo, hi)); }

    /** RTL {@code C+n} / {@code C-n} — column offset from the anchor (signed delta). */
    public static Prov Crel(int delta) { return new Prov(new FilterTerm.ColOffset(delta)); }

    /** RTL {@code C+lo..hi} — column range relative to the anchor. */
    public static Prov Crel(int lo, int hi) { return new Prov(new FilterTerm.ColRange(lo, hi)); }

    /** RTL {@code C+lo..*} — open-ended column range relative to the anchor. */
    public static Prov CrelFrom(int lo) { return new Prov(new FilterTerm.ColRange(lo, Integer.MAX_VALUE)); }

    /** RTL {@code Rn} — absolute row. */
    public static Prov R(int n) { return new Prov(new FilterTerm.RowExact(n)); }

    /** RTL {@code Ra..b} — absolute row range. */
    public static Prov R(int lo, int hi) { return new Prov(new FilterTerm.RowAbsoluteRange(lo, hi)); }

    /** RTL {@code R+n} / {@code R-n} — row offset from the anchor. */
    public static Prov Rrel(int delta) { return new Prov(new FilterTerm.RowOffset(delta)); }

    /** RTL {@code Pn} — absolute position. */
    public static Prov P(int n) { return new Prov(new FilterTerm.PosExact(n)); }

    /** RTL {@code Pa..b} — absolute position range. */
    public static Prov P(int lo, int hi) { return new Prov(new FilterTerm.PosRange(lo, hi)); }

    /** RTL {@code P+n} / {@code P-n} — position offset from the anchor. */
    public static Prov Prel(int delta) { return new Prov(new FilterTerm.PosOffset(delta)); }

    // ==== providers: content constraints ====

    /** RTL {@code #'tag'} constraint. */
    public static Prov tag(String tag) { return new Prov(new FilterTerm.Tagged("#" + tag)); }

    /** RTL {@code !#'tag'} constraint. */
    public static Prov notTag(String tag) { return new Prov(new FilterTerm.NotTagged("#" + tag)); }

    /** RTL {@code "regex"} item constraint. */
    public static Prov itemRe(String regex) { return new Prov(new FilterTerm.RegexMatched(regex)); }

    /** RTL {@code !"regex"} item constraint. */
    public static Prov itemNotRe(String regex) { return new Prov(new FilterTerm.NotRegexMatched(regex)); }

    /** RTL {@code BLANK} item constraint. */
    public static Prov itemBlank() { return new Prov(FilterTerm.Blank.INSTANCE); }

    /** RTL {@code !BLANK} item constraint. */
    public static Prov itemNotBlank() { return new Prov(FilterTerm.NotBlank.INSTANCE); }

    /** RTL {@code ~"sub"} item constraint. */
    public static Prov itemContains(String substring) { return new Prov(new FilterTerm.Contains(substring)); }

    /** RTL {@code !~"sub"} item constraint. */
    public static Prov itemNotContains(String substring) { return new Prov(new FilterTerm.NotContains(substring)); }

    // ==== context providers (RTL: 'text', @'ATTR'='VALUE') ====

    /** Context literal (RTL {@code 'EUR'}): VALUE under REC/JOIN, ATTRIBUTE otherwise. */
    public static Ctx lit(String text) { return new Ctx(text); }

    /** Constant attribute-value pair (RTL {@code @'ATTR'='VALUE'}). */
    public static CtxAvp ctxAvp(String attribute, String value) { return new CtxAvp(attribute, value); }

    /** Context string literal — see {@link #lit(String)}. */
    public record Ctx(String text) implements ProvArg {}

    /** Constant attribute-value pair — see {@link #ctxAvp(String, String)}. */
    public record CtxAvp(String attribute, String value) implements ProvArg {}

    // ==== actions (RTL: providers -> OP) ====

    /** RTL {@code (…)->REC}. */
    public static ActionSpec rec(ProvArg... providers) {
        return new ActionSpec(OperationType.REC, null, resolve(providers, OperationType.REC), null, null);
    }

    /** RTL {@code (…)->REC(n)} — with inline anchor position. */
    public static ActionSpec rec(int anchorPos, ProvArg... providers) {
        return new ActionSpec(OperationType.REC, null, resolve(providers, OperationType.REC), anchorPos, null);
    }

    /** RTL {@code (…)->REC('s')} — with inline split delimiter. */
    public static ActionSpec recSplit(String splitDelimiter, ProvArg... providers) {
        return new ActionSpec(OperationType.REC, null, resolve(providers, OperationType.REC), null, splitDelimiter);
    }

    /** RTL {@code prov->AVP}. */
    public static ActionSpec avp(Prov provider) {
        return new ActionSpec(OperationType.AVP, null,
                List.of(provider.spec(CellDerivedProviderKind.ATTR)), null, null);
    }

    /** RTL {@code 'NAME'->AVP} — constant attribute name. */
    public static ActionSpec avp(String literal) {
        return ActionSpec.avp(literal);
    }

    /** RTL {@code (…)->JOIN}. */
    public static ActionSpec join(ProvArg... providers) {
        return new ActionSpec(OperationType.JOIN, null, resolve(providers, OperationType.JOIN),
                null, null, Set.of(), false);
    }

    /** RTL {@code (…)->JOIN(k)} — with a key position. */
    public static ActionSpec join(int keyPosition, ProvArg... providers) {
        return new ActionSpec(OperationType.JOIN, null, resolve(providers, OperationType.JOIN),
                null, null, Set.of(keyPosition), false);
    }

    /** RTL {@code (…)->JOIN(k1,k2,…)} — with key positions. */
    public static ActionSpec join(Set<Integer> keyPositions, ProvArg... providers) {
        return new ActionSpec(OperationType.JOIN, null, resolve(providers, OperationType.JOIN),
                null, null, keyPositions, false);
    }

    /** RTL {@code (…)->FILL}. */
    public static ActionSpec fill(ProvArg... providers) { return fill("", providers); }

    /** RTL {@code (…)->FILL('d')}. */
    public static ActionSpec fill(String delimiter, ProvArg... providers) {
        return new ActionSpec(OperationType.FILL, delimiter, resolve(providers, OperationType.FILL), null, null);
    }

    /** RTL {@code (…)->PREFIX}. */
    public static ActionSpec prefix(ProvArg... providers) { return prefix("", providers); }

    /** RTL {@code (…)->PREFIX('d')}. */
    public static ActionSpec prefix(String delimiter, ProvArg... providers) {
        return new ActionSpec(OperationType.PREFIX, delimiter, resolve(providers, OperationType.PREFIX), null, null);
    }

    /** RTL {@code (…)->SUFFIX}. */
    public static ActionSpec suffix(ProvArg... providers) { return suffix("", providers); }

    /** RTL {@code (…)->SUFFIX('d')}. */
    public static ActionSpec suffix(String delimiter, ProvArg... providers) {
        return new ActionSpec(OperationType.SUFFIX, delimiter, resolve(providers, OperationType.SUFFIX), null, null);
    }

    // ==== provider resolution (mirrors the RTL compiler's kind inference) ====

    private static List<ProviderSpec> resolve(ProvArg[] providers, OperationType op) {
        var result = new ArrayList<ProviderSpec>(providers.length);
        for (ProvArg arg : providers) {
            result.add(switch (arg) {
                case Prov p   -> p.spec(kindFor(op));
                case Ctx c    -> (op == OperationType.REC || op == OperationType.JOIN)
                        ? ProviderSpec.ctxVal(c.text())
                        : ProviderSpec.ctxAttr(c.text());
                case CtxAvp x -> ProviderSpec.ctxAvp(x.attribute(), x.value());
            });
        }
        return List.copyOf(result);
    }

    private static CellDerivedProviderKind kindFor(OperationType op) {
        return switch (op) {
            case REC, JOIN -> CellDerivedProviderKind.VAL;
            case AVP       -> CellDerivedProviderKind.ATTR;
            default        -> CellDerivedProviderKind.UNRESTRICTED;
        };
    }
}
