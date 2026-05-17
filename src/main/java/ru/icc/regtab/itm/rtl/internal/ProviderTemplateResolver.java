package ru.icc.regtab.itm.rtl.internal;

import ru.icc.regtab.itm.atp.spec.ItemDerivationDirective;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.model.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.rtl.RTLParser;
import ru.icc.regtab.itm.rtl.RtlCompileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves RTL provider specifications into {@link ProviderSpec}.
 *
 * <p>Each {@code tblProvSpec} has an optional traversal-order mark and spatial/content constraints:
 * <ul>
 *   <li>(no mark) → ROW_MAJOR</li>
 *   <li>{@code -}  → REVERSE_ROW_MAJOR</li>
 *   <li>{@code ^}  → COLUMN_MAJOR</li>
 *   <li>{@code -^} → REVERSE_COLUMN_MAJOR</li>
 * </ul>
 *
 * <p>Named spatial constraints and their base filter conditions:
 * <ul>
 *   <li>LT: sameSubrow(a) &amp;&amp; col &lt; col(a)</li>
 *   <li>RT: sameSubrow(a) &amp;&amp; col &gt; col(a)</li>
 *   <li>AV: sameSubcol(a) &amp;&amp; row &lt; row(a)</li>
 *   <li>BW: sameSubcol(a) &amp;&amp; row &gt; row(a)</li>
 *   <li>ROW: sameRow(a) &amp;&amp; !sameCell(a)</li>
 *   <li>COL: sameCol(a) &amp;&amp; !sameCell(a)</li>
 *   <li>SR: sameSubrow(a) &amp;&amp; !sameCell(a)</li>
 *   <li>SC: sameSubcol(a) &amp;&amp; !sameCell(a)</li>
 *   <li>ST: sameSubtable(a) &amp;&amp; !sameCell(a)</li>
 *   <li>TAB: !sameCell(a)</li>
 *   <li>CL: sameCell(a)</li>
 * </ul>
 * Additional col/row/pos and content constraints are AND-ed with the named base condition.
 */
final class ProviderTemplateResolver {

    private ProviderTemplateResolver() {}

    /**
     * Builds a {@link ProviderSpec} from an RTL {@code tblProvSpec} context.
     * The provider kind is inferred from the action type and anchor item type.
     */
    static ProviderSpec resolve(RTLParser.TblProvSpecContext ctx,
                                RTLParser.OpContext op,
                                ItemDerivationDirective anchorType) {
        TraversalOrder order = parseTraversalOrder(ctx.traversalOrderMark());
        int cardinality = parseCardinality(ctx.cardinality());
        ItemFilterCondition condition = buildCondition(ctx);
        CellDerivedProviderKind kind = inferKind(op, anchorType);
        int actualCardinality = (kind == CellDerivedProviderKind.ATTR) ? 1 : cardinality;
        return new ProviderSpec(actualCardinality, order, condition, kind, null);
    }

    /** Builds a {@link ProviderSpec} with UNRESTRICTED kind (for inherited/context action specs). */
    static ProviderSpec resolve(RTLParser.TblProvSpecContext ctx) {
        TraversalOrder order = parseTraversalOrder(ctx.traversalOrderMark());
        int cardinality = parseCardinality(ctx.cardinality());
        ItemFilterCondition condition = buildCondition(ctx);
        return new ProviderSpec(cardinality, order, condition,
                CellDerivedProviderKind.UNRESTRICTED, null);
    }

    // --- Traversal order ---

    private static TraversalOrder parseTraversalOrder(RTLParser.TraversalOrderMarkContext ctx) {
        if (ctx == null)                          return TraversalOrder.ROW_MAJOR;
        if (ctx.columnMajor()        != null)     return TraversalOrder.COLUMN_MAJOR;
        if (ctx.reverseRowMajor()    != null)     return TraversalOrder.REVERSE_ROW_MAJOR;
        if (ctx.reverseColumnMajor() != null)     return TraversalOrder.REVERSE_COLUMN_MAJOR;
        throw new RtlCompileException("Unknown traversal order mark");
    }

    // --- Kind inference ---

    private static CellDerivedProviderKind inferKind(RTLParser.OpContext op,
                                                     ItemDerivationDirective anchorType) {
        if (op == null || anchorType == null) return CellDerivedProviderKind.UNRESTRICTED;
        if (op.recOp() != null || op.CONCAT() != null) return CellDerivedProviderKind.VAL;
        if (op.AVP() != null)                        return CellDerivedProviderKind.ATTR;
        return CellDerivedProviderKind.UNRESTRICTED;
    }

    // --- Cardinality ---

    private static int parseCardinality(RTLParser.CardinalityContext ctx) {
        if (ctx == null)              return 1;
        if (ctx.MULT() != null)       return ProviderSpec.UNBOUNDED;
        return Integer.parseInt(ctx.INT().getText());
    }

    // --- Condition building ---

    private static ItemFilterCondition buildCondition(RTLParser.TblProvSpecContext ctx) {
        if (ctx.spatConstr() != null) {
            List<ItemFilterCondition> parts = new ArrayList<>();
            addSpatConstrParts(ctx.spatConstr(), parts);
            return andAll(parts);
        }
        if (ctx.constraints() != null) return buildConstraints(ctx.constraints());
        return (a, c) -> true;
    }

    private static ItemFilterCondition buildConstraints(RTLParser.ConstraintsContext ctx) {
        return orAll(ctx.orGroup().stream().map(ProviderTemplateResolver::buildOrGroup).toList());
    }

    private static ItemFilterCondition buildOrGroup(RTLParser.OrGroupContext ctx) {
        return andAll(ctx.baseConstr().stream().map(ProviderTemplateResolver::buildBaseConstr).toList());
    }

    private static ItemFilterCondition buildBaseConstr(RTLParser.BaseConstrContext ctx) {
        if (ctx.constraints() != null) return buildConstraints(ctx.constraints());
        return buildConstr(ctx.constr());
    }

    private static ItemFilterCondition buildConstr(RTLParser.ConstrContext ctx) {
        List<ItemFilterCondition> parts = new ArrayList<>();
        if (ctx.spatConstr() != null) addSpatConstrParts(ctx.spatConstr(), parts);
        else if (ctx.contConstr() != null) parts.add(buildContentConstraint(ctx.contConstr()));
        return andAll(parts);
    }

    private static void addSpatConstrParts(RTLParser.SpatConstrContext ctx,
                                           List<ItemFilterCondition> parts) {
        // Named spatial constraint → base condition
        ItemFilterCondition named = namedSpatCondition(ctx);
        if (named != null) parts.add(named);

        // Positional constraints
        if (ctx.col() != null) parts.add(colFilter(ctx.col()));
        if (ctx.row() != null) parts.add(rowFilter(ctx.row()));
        if (ctx.pos() != null) parts.add(posFilter(ctx.pos()));
    }

    /** Returns the base filter condition for a named spatial constraint token, or null for col/row/pos. */
    private static ItemFilterCondition namedSpatCondition(RTLParser.SpatConstrContext ctx) {
        if (ctx.LEFT_OF()   != null) return (a, c) -> c.sameSubrow(a)   && c.cell().col() < a.cell().col();
        if (ctx.RIGHT_OF()  != null) return (a, c) -> c.sameSubrow(a)   && c.cell().col() > a.cell().col();
        if (ctx.ABOVE()     != null) return (a, c) -> c.sameSubcol(a)   && c.cell().row() < a.cell().row();
        if (ctx.BELOW()     != null) return (a, c) -> c.sameSubcol(a)   && c.cell().row() > a.cell().row();
        if (ctx.ROW()       != null) return (a, c) -> c.sameRow(a)      && !c.sameCell(a);
        if (ctx.COLUMN()    != null) return (a, c) -> c.sameCol(a)      && !c.sameCell(a);
        if (ctx.SUBROW()    != null) return (a, c) -> c.sameSubrow(a)   && !c.sameCell(a);
        if (ctx.SUBCOLUMN() != null) return (a, c) -> c.sameSubcol(a)   && !c.sameCell(a);
        if (ctx.SUBTABLE()  != null) return (a, c) -> c.sameSubtable(a) && !c.sameCell(a);
        if (ctx.TABLE()     != null) return (a, c) -> !c.sameCell(a);
        if (ctx.CELL()      != null) return (a, c) -> c.sameCell(a);
        return null; // col/row/pos — no named base condition
    }

    // --- Spatial filter builders ---

    private static ItemFilterCondition colFilter(RTLParser.ColContext ctx) {
        if (ctx.range() != null) return rangeFilter(ctx.range(),
                (a, c) -> c.cell().col(), (a, c) -> a.cell().col());
        if (ctx.offset() != null) {
            int delta = parseOffset(ctx.offset());
            return (a, c) -> c.cell().col() == a.cell().col() + delta;
        }
        int abs = Integer.parseInt(ctx.INT().getText());
        return (a, c) -> c.cell().col() == abs;
    }

    private static ItemFilterCondition rowFilter(RTLParser.RowContext ctx) {
        if (ctx.range() != null) return rangeFilter(ctx.range(),
                (a, c) -> c.cell().row(), (a, c) -> a.cell().row());
        if (ctx.offset() != null) {
            int delta = parseOffset(ctx.offset());
            return (a, c) -> c.cell().row() == a.cell().row() + delta;
        }
        int abs = Integer.parseInt(ctx.INT().getText());
        return (a, c) -> c.cell().row() == abs;
    }

    private static ItemFilterCondition posFilter(RTLParser.PosContext ctx) {
        if (ctx.range() != null) return rangeFilter(ctx.range(),
                (a, c) -> c.index(), (a, c) -> a.index());
        if (ctx.offset() != null) {
            int delta = parseOffset(ctx.offset());
            return (a, c) -> c.index() == a.index() + delta;
        }
        int abs = Integer.parseInt(ctx.INT().getText());
        return (a, c) -> c.index() == abs;
    }

    @FunctionalInterface
    private interface IntExtractor {
        int get(ru.icc.regtab.itm.model.semantics.item.CellDerivedItem anchor,
                ru.icc.regtab.itm.model.semantics.item.CellDerivedItem candidate);
    }

    private static ItemFilterCondition rangeFilter(
            RTLParser.RangeContext ctx, IntExtractor candVal, IntExtractor anchorVal) {
        boolean hiOpen = ctx.end() == null;
        int lo = boundaryValue(ctx.start(), 0);
        int hi = hiOpen ? Integer.MAX_VALUE : boundaryValue(ctx.end(), Integer.MAX_VALUE);
        boolean loRelative = ctx.start().offset() != null;
        boolean hiRelative = !hiOpen && ctx.end().offset() != null;
        int loDelta = loRelative ? parseOffset(ctx.start().offset()) : lo;
        int hiDelta = hiRelative ? parseOffset(ctx.end().offset())   : hi;
        boolean loAbs = !loRelative;
        boolean hiAbs = !hiRelative;
        return (a, c) -> {
            int v   = candVal.get(a, c);
            int lo2 = loAbs ? loDelta : anchorVal.get(a, c) + loDelta;
            int hi2 = hiAbs ? hiDelta : anchorVal.get(a, c) + hiDelta;
            return v >= lo2 && v <= hi2;
        };
    }

    private static int boundaryValue(RTLParser.StartContext ctx, int defaultVal) {
        if (ctx.offset() != null) return parseOffset(ctx.offset());
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        return defaultVal;
    }

    private static int boundaryValue(RTLParser.EndContext ctx, int defaultVal) {
        if (ctx.offset() != null) return parseOffset(ctx.offset());
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        return defaultVal;
    }

    private static int parseOffset(RTLParser.OffsetContext ctx) {
        int n = Integer.parseInt(ctx.INT().getText());
        return ctx.MINUS() != null ? -n : n;
    }

    // --- Content constraint builders ---

    private static ItemFilterCondition buildContentConstraint(RTLParser.ContConstrContext ctx) {
        if (ctx.regex()   != null) return regexFilter(ctx.regex());
        if (ctx.blank()   != null) return blankFilter(ctx.blank());
        if (ctx.tag()     != null) return tagFilter(ctx.tag());
        if (ctx.sameStr() != null) return (a, c) -> c.sameStr(a);
        throw new RtlCompileException("Unknown content constraint");
    }

    private static ItemFilterCondition regexFilter(RTLParser.RegexContext ctx) {
        String pattern = StringExtractorFactory.parseStringLiteral(ctx.STRING().getText());
        boolean negated = ctx.EXCLAMATION() != null;
        return negated
                ? (a, c) -> !c.str().matches(pattern)
                : (a, c) ->  c.str().matches(pattern);
    }

    private static ItemFilterCondition blankFilter(RTLParser.BlankContext ctx) {
        boolean negated = ctx.EXCLAMATION() != null;
        return negated
                ? (a, c) -> !c.blankStr()
                : (a, c) ->  c.blankStr();
    }

    private static ItemFilterCondition tagFilter(RTLParser.TagContext ctx) {
        List<String> tags = ctx.TAG().stream()
                .map(t -> t.getText())
                .toList();
        return (a, c) -> tags.stream().anyMatch(c::hasTag);
    }

    // --- Combining predicates ---

    private static ItemFilterCondition andAll(List<ItemFilterCondition> parts) {
        if (parts.isEmpty()) return (a, c) -> true;
        if (parts.size() == 1) return parts.get(0);
        ItemFilterCondition result = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            final ItemFilterCondition prev = result;
            final ItemFilterCondition next = parts.get(i);
            result = (a, c) -> prev.test(a, c) && next.test(a, c);
        }
        return result;
    }

    private static ItemFilterCondition orAll(List<ItemFilterCondition> parts) {
        if (parts.isEmpty()) return (a, c) -> true;
        if (parts.size() == 1) return parts.get(0);
        ItemFilterCondition result = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            final ItemFilterCondition prev = result;
            final ItemFilterCondition next = parts.get(i);
            result = (a, c) -> prev.test(a, c) || next.test(a, c);
        }
        return result;
    }
}
