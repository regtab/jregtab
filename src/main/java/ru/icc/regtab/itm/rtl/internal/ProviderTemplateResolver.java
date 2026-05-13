package ru.icc.regtab.itm.rtl.internal;

import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;
import ru.icc.regtab.itm.rtl.RTLParser;
import ru.icc.regtab.itm.rtl.RtlCompileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves RTL provider templates (LW/RW/UW/DW/RM/CM/CL) with their spatial
 * and content constraints into a {@link ProviderSpec}.
 *
 * <p>Default filter conditions per template (from RTL grammar comments):
 * <ul>
 *   <li>LW: sameSubrow(a) &amp;&amp; col(c) &lt; col(a)</li>
 *   <li>RW: sameSubrow(a) &amp;&amp; col(c) &gt; col(a)</li>
 *   <li>UW: sameSubtable(a) &amp;&amp; sameCol(a) &amp;&amp; row(c) &lt; row(a)</li>
 *   <li>DW: sameSubtable(a) &amp;&amp; sameCol(a) &amp;&amp; row(c) &gt; row(a)</li>
 *   <li>RM: sameSubrow(a) &amp;&amp; !sameCell(a)</li>
 *   <li>CM: sameSubtable(a) &amp;&amp; sameCol(a) &amp;&amp; !sameCell(a)</li>
 *   <li>CL: sameCell(a)</li>
 * </ul>
 *
 * <p>Spatial constraints modify the default condition:
 * <ul>
 *   <li>col: replaces sameCol(a) for UW/DW/CM; replaces sameCell(a) for CL</li>
 *   <li>row: replaces sameSubrow(a) for LW/RW/RM; replaces sameSubtable(a) for UW/DW/CM;
 *            replaces sameCell(a) for CL</li>
 *   <li>pos: replaces sameCell(a) for CL</li>
 *   <li>st: replaces sameSubrow(a) with sameSubtable(a) for LW/RW/RM;
 *           removes sameCol(a) restriction for UW/DW/CM;
 *           replaces sameCell(a) with sameSubtable(a) for CL</li>
 * </ul>
 * Note: !sameCell(a) in RM/CM is essential and is always included.
 * For LW/RW/UW/DW, spatial direction guarantees different cells so !sameCell is omitted.
 */
final class ProviderTemplateResolver {

    private ProviderTemplateResolver() {}

    enum Template { LW, RW, UW, DW, RM, CM, CL }

    static Template parseTemplate(RTLParser.ProvTemplateContext ctx) {
        if (ctx.LEFTWARD()     != null) return Template.LW;
        if (ctx.RIGHTWARD()    != null) return Template.RW;
        if (ctx.UPWARD()       != null) return Template.UW;
        if (ctx.DOWNWARD()     != null) return Template.DW;
        if (ctx.ROW_MAJOR()    != null) return Template.RM;
        if (ctx.COLUMN_MAJOR() != null) return Template.CM;
        if (ctx.CELL()         != null) return Template.CL;
        throw new RtlCompileException("Unknown provider template");
    }

    static TraversalOrder traversalOrderFor(Template t) {
        return switch (t) {
            case LW -> TraversalOrder.REVERSE_ROW_MAJOR;
            case RW -> TraversalOrder.ROW_MAJOR;
            case UW -> TraversalOrder.REVERSE_COLUMN_MAJOR;
            case DW -> TraversalOrder.COLUMN_MAJOR;
            case RM -> TraversalOrder.ROW_MAJOR;
            case CM -> TraversalOrder.COLUMN_MAJOR;
            case CL -> TraversalOrder.ROW_MAJOR;
        };
    }

    /**
     * Builds a {@link ProviderSpec} from an RTL {@code tblProvSpec} context.
     * The provider type is always UNRESTRICTED (Phase 1 simplification).
     */
    static ProviderSpec resolve(RTLParser.TblProvSpecContext ctx) {
        Template template = parseTemplate(ctx.provTemplate());
        TraversalOrder order = traversalOrderFor(template);
        int cardinality = parseCardinality(ctx.cardinality());
        ItemFilterCondition condition = buildCondition(template, ctx.constraints());
        return new ProviderSpec(cardinality, order, condition,
                ru.icc.regtab.itm.model.semantics.provider.CellDerivedProviderKind.UNRESTRICTED,
                null);
    }

    // --- Cardinality ---

    private static int parseCardinality(RTLParser.CardinalityContext ctx) {
        if (ctx == null) return ProviderSpec.UNBOUNDED;
        return Integer.parseInt(ctx.INT().getText());
    }

    // --- Condition building ---

    private static ItemFilterCondition buildCondition(
            Template template, RTLParser.ConstraintsContext constraintsCtx) {

        RTLParser.ColContext colCtx = null;
        RTLParser.RowContext rowCtx = null;
        RTLParser.PosContext posCtx = null;
        boolean st = false;
        List<ItemFilterCondition> contentParts = new ArrayList<>();

        if (constraintsCtx != null) {
            for (var constr : constraintsCtx.constr()) {
                var spat = constr.spatConstr();
                var cont = constr.contConstr();
                if (spat != null) {
                    if (spat.col() != null) colCtx = spat.col();
                    else if (spat.row() != null) rowCtx = spat.row();
                    else if (spat.pos() != null) posCtx = spat.pos();
                    else if (spat.st() != null) st = true;
                } else if (cont != null) {
                    contentParts.add(buildContentConstraint(cont));
                }
            }
        }

        List<ItemFilterCondition> parts = new ArrayList<>();
        buildTemplateParts(template, colCtx, rowCtx, posCtx, st, parts);
        parts.addAll(contentParts);
        return andAll(parts);
    }

    private static void buildTemplateParts(
            Template t,
            RTLParser.ColContext col,
            RTLParser.RowContext row,
            RTLParser.PosContext pos,
            boolean st,
            List<ItemFilterCondition> parts) {

        switch (t) {
            case LW -> {
                // st expands scope to sameSubtable; else sameSubrow (replaced by row if present)
                if (st)               parts.add((a, c) -> c.sameSubtable(a));
                else if (row == null) parts.add((a, c) -> c.sameSubrow(a));
                else                  parts.add(rowFilter(row));
                if (col != null) parts.add(colFilter(col));
                parts.add((a, c) -> c.cell().col() < a.cell().col());
            }
            case RW -> {
                if (st)               parts.add((a, c) -> c.sameSubtable(a));
                else if (row == null) parts.add((a, c) -> c.sameSubrow(a));
                else                  parts.add(rowFilter(row));
                if (col != null) parts.add(colFilter(col));
                parts.add((a, c) -> c.cell().col() > a.cell().col());
            }
            case UW -> {
                if (row == null) parts.add((a, c) -> c.sameSubtable(a));
                else             parts.add(rowFilter(row));
                // st removes the sameCol restriction; col constraint replaces it if present
                if (!st) {
                    if (col == null) parts.add((a, c) -> c.sameCol(a));
                    else             parts.add(colFilter(col));
                } else if (col != null) {
                    parts.add(colFilter(col));
                }
                parts.add((a, c) -> c.cell().row() < a.cell().row());
            }
            case DW -> {
                if (row == null) parts.add((a, c) -> c.sameSubtable(a));
                else             parts.add(rowFilter(row));
                if (!st) {
                    if (col == null) parts.add((a, c) -> c.sameCol(a));
                    else             parts.add(colFilter(col));
                } else if (col != null) {
                    parts.add(colFilter(col));
                }
                parts.add((a, c) -> c.cell().row() > a.cell().row());
            }
            case RM -> {
                if (st)               parts.add((a, c) -> c.sameSubtable(a));
                else if (row == null) parts.add((a, c) -> c.sameSubrow(a));
                else                  parts.add(rowFilter(row));
                if (col != null) parts.add(colFilter(col));
                parts.add((a, c) -> !c.sameCell(a));
            }
            case CM -> {
                if (row == null) parts.add((a, c) -> c.sameSubtable(a));
                else             parts.add(rowFilter(row));
                if (!st) {
                    if (col == null) parts.add((a, c) -> c.sameCol(a));
                    else             parts.add(colFilter(col));
                } else if (col != null) {
                    parts.add(colFilter(col));
                }
                parts.add((a, c) -> !c.sameCell(a));
            }
            case CL -> {
                // Any spatial constraint replaces sameCell(a)
                boolean hasSpatial = col != null || row != null || pos != null || st;
                if (!hasSpatial) {
                    parts.add((a, c) -> c.sameCell(a));
                } else {
                    if (st)          parts.add((a, c) -> c.sameSubtable(a));
                    if (col != null) parts.add(colFilter(col));
                    if (row != null) parts.add(rowFilter(row));
                    if (pos != null) parts.add(posFilter(pos));
                }
            }
        }
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
        int lo = boundaryValue(ctx.start(), anchorVal, 0);
        int hi = boundaryValue(ctx.end(),   anchorVal, Integer.MAX_VALUE);
        boolean loRelative = ctx.start().offset() != null;
        boolean hiRelative = ctx.end().offset()   != null;
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

    private static int boundaryValue(RTLParser.StartContext ctx,
                                     IntExtractor anchor, int defaultVal) {
        if (ctx.offset() != null) return parseOffset(ctx.offset());
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        return defaultVal;
    }

    private static int boundaryValue(RTLParser.EndContext ctx,
                                     IntExtractor anchor, int defaultVal) {
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
        if (ctx.regex() != null) return regexFilter(ctx.regex());
        if (ctx.blank() != null) return blankFilter(ctx.blank());
        if (ctx.tag()   != null) return tagFilter(ctx.tag());
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
        return (a, c) -> tags.stream().allMatch(c::hasTag);
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
}
