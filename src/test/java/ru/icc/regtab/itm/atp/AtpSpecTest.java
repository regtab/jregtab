package ru.icc.regtab.itm.atp;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.atp.spec.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ATP model spec classes.
 */
class AtpSpecTest {

    @Test
    void quantifierBounds() {
        assertEquals(0, Quantifier.zeroOrOne().min());
        assertEquals(1, Quantifier.zeroOrOne().max());

        assertEquals(1, Quantifier.one().min());
        assertEquals(1, Quantifier.one().max());

        assertEquals(1, Quantifier.oneOrMore().min());
        assertEquals(Quantifier.UNBOUNDED, Quantifier.oneOrMore().max());

        assertEquals(0, Quantifier.zeroOrMore().min());
        assertEquals(Quantifier.UNBOUNDED, Quantifier.zeroOrMore().max());

        assertEquals(5, Quantifier.exactly(5).min());
        assertEquals(5, Quantifier.exactly(5).max());
    }

    @Test
    void quantifierExactlyRejectsSmallN() {
        assertThrows(IllegalArgumentException.class, () -> Quantifier.exactly(1));
        assertThrows(IllegalArgumentException.class, () -> Quantifier.exactly(0));
    }

    @Test
    void itemDerivationDirectiveToItemType() {
        assertEquals(ru.icc.regtab.itm.model.semantics.item.ItemType.VALUE,
                ItemDerivationDirective.VAL.toItemType());
        assertEquals(ru.icc.regtab.itm.model.semantics.item.ItemType.ATTRIBUTE,
                ItemDerivationDirective.ATTR.toItemType());
        assertEquals(ru.icc.regtab.itm.model.semantics.item.ItemType.AUXILIARY,
                ItemDerivationDirective.AUX.toItemType());
        assertThrows(IllegalStateException.class, () -> ItemDerivationDirective.SKIP.toItemType());
    }

    @Test
    void atomicContentSpecConvenience() {
        var val = AtomicContentSpec.val();
        assertEquals(ItemDerivationDirective.VAL, val.idd());
        assertNull(val.extractor());
        assertTrue(val.tags().isEmpty());
        assertTrue(val.actions().isEmpty());

        var skip = AtomicContentSpec.skip();
        assertEquals(ItemDerivationDirective.SKIP, skip.idd());

        var tagged = AtomicContentSpec.valTagged("#L1");
        assertEquals(java.util.List.of("#L1"), tagged.tags());
    }

    @Test
    void delimitedContentSpecRejectsEmptyDelimiter() {
        assertThrows(IllegalArgumentException.class,
                () -> new DelimitedContentSpec("", AtomicContentSpec.val()));
    }

    @Test
    void compoundContentSpecRequiresSegments() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompoundContentSpec(List.of()));
    }

    @Test
    void stringExtractors() {
        assertEquals("hello", StringExtractor.identity().apply("hello"));
        assertEquals("ell", StringExtractor.substring(1, 4).apply("hello"));
        assertEquals("hXllo", StringExtractor.replace("e", "X").apply("hello"));
        assertEquals("hello", StringExtractor.trim().apply("  hello  "));
    }

    @Test
    void cellMatchConditionCombinations() {
        var cond1 = new CellMatchCondition(c -> c.text().startsWith("A"));
        var cond2 = new CellMatchCondition(c -> c.text().length() > 3);
        var combined = cond1.and(cond2);
        var negated = cond1.negate();
        // These are just structural tests — predicates tested in matcher tests
        assertNotNull(combined);
        assertNotNull(negated);
    }

    @Test
    void patternHierarchyConstruction() {
        var cellVal = CellPattern.of(AtomicContentSpec.val());
        var cellSkip = CellPattern.skip();
        var subrow = SubrowPattern.of(cellVal, cellSkip);
        var row = RowPattern.of(Quantifier.oneOrMore(), cellVal, cellSkip);
        var subtable = SubtablePattern.of(Quantifier.oneOrMore(), row);
        var table = TablePattern.of(subtable);

        assertEquals(1, table.subtablePatterns().size());
        assertEquals(1, subtable.rowPatterns().size());
        assertEquals(1, row.subrowPatterns().size());
        assertEquals(2, row.subrowPatterns().getFirst().cellPatterns().size());
    }

    @Test
    void actionSpecConvenience() {
        var ps = ProviderSpec.of((a, c) -> true);
        var rec = ActionSpec.rec(ps);
        assertEquals(OperationType.REC, rec.operationType());
        assertEquals(1, rec.providers().size());

        var fill = ActionSpec.fill(", ", ps);
        assertEquals(OperationType.FILL, fill.operationType());
        assertEquals(", ", fill.delimiter());
    }
}
