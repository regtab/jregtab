# Examples

Three worked examples drawn from the Foofah benchmark and the paper's illustrative example.
For each task the ATP pattern and its RTL equivalent are shown side by side.

---

## Example 1 — Task 001: repeating subtables, flat record

**Input layout** (Foofah task 01, variant 1):

```
name_1   | name_2 | name_3
value_1  | value_2| value_3| value_4
```

Each pair of rows forms one subtable. The first row carries one anchor value and two plain values;
the second row carries four plain values. The anchor value in the first row establishes a record
that collects all same-subtable values.

**Schema:** single anonymous attribute `$a_1` (all cells in one subtable become one record).

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;

TablePattern pattern = TablePattern.of(
    SubtablePattern.of(Quantifier.oneOrMore(),
        RowPattern.of(
            CellPattern.of(AtomicContentSpec.val(
                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED,
                               ItemFilterConditionSpec.sameSubtable()))
            )),
            CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val()),
            CellPattern.skip(Quantifier.oneOrMore())
        ),
        RowPattern.of(
            CellPattern.skip(),
            CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val()),
            CellPattern.skip(Quantifier.oneOrMore())
        )
    )
);
```

### RTL equivalent

```
{ [ [VAL : ST*->REC] [VAL]{2} []+ ]
  [ []               [VAL]{4} []+ ] }+
```

### How it works

- `{ … }+` — one or more subtables.
- First row: anchor cell `[VAL : ST*->REC]` — a VAL item with a `REC` action using an unbounded `ST` provider.  
  At match time the anchor item's `rec` action will collect **all** VAL items in the same subtable (`ST*`) to build one record.
- `[VAL]{2}` — exactly two more VAL cells (skip-qualified patterns absorb trailing cells `[]+`).
- Second row: `[]` skips the first cell; `[VAL]{4}` — four more VAL cells.
- All four plain VAL cells become field values in the record anchored by the first row's VAL.

### Running the test

```bash
mvn test -Dtest="AtpTask001Test"
mvn test -Dtest="RtlTask001Test"
```

---

## Example 2 — Illustrative example (paper, Section VI): cross-row providers

This example is taken directly from the paper. The table lists numbers of airline departures (`ND`)
from airports by month. Target schema: `⟨ND, AIRLINE, AIRPORT, MON⟩`.

**Input table:**

```
        | CA    | HU
IKT     | 0 Jan | 8 Feb
SVO     | 31 Jan| 40 Feb
```

- Row 0: header with airline codes.
- Rows 1–2: airport code in column 0, then compound `"ND MON"` body cells.

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.interpret.*;

TablePattern pattern = TablePattern.of(
    SubtablePattern.of(
        // Header row: skip empty cell + one-or-more airline cells
        RowPattern.of(
            CellPattern.skip(),
            CellPattern.of(Quantifier.oneOrMore(),
                AtomicContentSpec.val(ActionSpec.avp("AIRLINE"))
            )
        ),
        // Data rows (one-or-more): airport cell + one-or-more compound body cells
        RowPattern.of(Quantifier.oneOrMore(),
            CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
            CellPattern.of(Quantifier.oneOrMore(),
                CompoundContentSpec.of(
                    AtomicContentSpec.val(
                        ActionSpec.rec(1,
                            ItemFilterConditionSpec.sameCol(),   // AIRLINE (column-major, header)
                            ItemFilterConditionSpec.sameRow(),   // AIRPORT (same row, leftmost)
                            ItemFilterConditionSpec.sameCell()   // MON (same compound cell)
                        ),
                        ActionSpec.avp("ND")
                    ),
                    CompoundContentSpec.Segment.of(" ",
                        AtomicContentSpec.val(ActionSpec.avp("MON"))
                    )
                )
            )
        )
    )
);
```

### RTL equivalent

```
[ [] [VAL : ('AIRLINE')->AVP]+ ]
[ [VAL : ('AIRPORT')->AVP]
  [VAL " " VAL : (^COL, SR{1}(C+0), CL)->REC(1), ('ND')->AVP]+ ]+
```

> Note: the ATP version above is the canonical form from `AtpIllustrativeExampleTest`.
> The RTL approximation is illustrative; the exact encoding can differ for compound cells
> with multiple action specs.

### How it works

- The header row derives ATTR items tagged `AIRLINE` for each airline column.
- Each data row derives one ATTR item tagged `AIRPORT` from the first cell.
- Each compound body cell (`"0 Jan"`) splits into two items: `ND` (the number) and `MON` (the month).
- The `rec` action on the `ND` item uses three providers:
  - `sameCol()` column-major — finds the AIRLINE ATTR in the header row of the same column.
  - `sameRow()` — finds the AIRPORT ATTR in the leftmost cell of the same row.
  - `sameCell()` — finds the MON item within the same cell.
- `ActionSpec.rec(1, …)` adds `AnchorAttributeAtPosition(1)` post-processing:
  the attribute at position 1 in the sequence becomes the schema's first attribute.

### Running the test

```bash
mvn test -Dtest="AtpIllustrativeExampleTest"
```

---

## Example 3 — Task 002: cell match condition, string extraction, cardinality

**Input layout** (Foofah task 02, variant 1):

```
Header_A  | (skip)
Header_B  | (skip)
Value_1   | Value_2
Value_3   | Value_4
(blank)   | (skip)
```

Each subtable has exactly 2 normalised header rows, one-or-more data rows, and an optional
blank-row footer. The anchor (non-blank first cell in each data row) collects two header values
from the same subcolumn and one value from the same subrow.

**Schema:** `⟨Header_A, Header_B, Value⟩` (headers become attribute names via `REC(2)`).

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;

var notBlank = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
var blank    = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

var sameSubcol = ItemFilterConditionSpec.sameSubcol();
var sameSubrow = ItemFilterConditionSpec.sameSubrow();

TablePattern pattern = TablePattern.of(
    SubtablePattern.of(Quantifier.oneOrMore(),
        // 2 header rows with whitespace-normalised values
        RowPattern.of(Quantifier.exactly(2),
            CellPattern.of(AtomicContentSpec.val(StringExtractor.WhitespaceNormalized.INSTANCE)),
            CellPattern.skip()
        ),
        // 1+ data rows: non-blank anchor + one plain value
        RowPattern.of(Quantifier.oneOrMore(),
            CellPattern.of(notBlank, Quantifier.one(), AtomicContentSpec.val(
                ActionSpec.rec(2,
                    ProviderSpec.val(2, sameSubcol),    // two headers from same subcolumn
                    ProviderSpec.val(1, sameSubrow)     // one value from same subrow
                )
            )),
            CellPattern.of(AtomicContentSpec.val())
        ),
        // Optional blank-row footer
        RowPattern.of(Quantifier.zeroOrOne(),
            CellPattern.of(blank, Quantifier.one(), null),
            CellPattern.skip()
        )
    )
);
```

### RTL equivalent

```
{ [ [VAL=NORM] [] ]{2}
  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
  [ [BLANK] [] ]? }+
```

### How it works

- `{2}` — the header row pattern repeats exactly twice.
- `VAL=NORM` — derive a value item with whitespace normalisation applied to the cell text.
- `[!BLANK ? VAL : …]` — the anchor cell pattern is guarded: only match non-blank cells.
- `(SC{2}, SR)->REC(2)`:
  - `SC{2}` — collect up to 2 items from the same subcolumn (the two header rows above).
  - `SR` — collect 1 item from the same subrow (the adjacent value cell).
  - `REC(2)` — the item at position 2 in the sequence (the second header) becomes the attribute name.
- `[ [BLANK] [] ]?` — an optional footer row; the first cell must be blank (`BLANK` is the condition-only guard).

### Running the test

```bash
mvn test -Dtest="AtpTask002Test"
mvn test -Dtest="RtlTask002Test"
```

---

---

## Named fragments (de-duplicating repeated sub-patterns)

When the same sub-pattern appears in multiple non-adjacent positions, **named fragment
definitions** eliminate the repetition. Fragments are declared in the RTL preamble
(before the first `[` or `{`) and referenced by `[$N]` or `{$N}` at the appropriate level.

**Task 116** — environmental monitoring table with repeating column groups.
Without fragments, `[VAL: -AV->PREFIX(', ')]` appears five times and
`[VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]` appears eight times.

```
$V1=[VAL: -AV->PREFIX(', ')]
$V2=[VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]
[ []+ ]
[ [] [VAL: 'TERRITORY'->AVP]+ ]
[ [AUX]+ ]
[ 'LOCATION'->AVP [] [$V1]{4} [VAL] []
                     [VAL] [$V1] [VAL]
                     [$V1] [VAL] []
                     { [VAL] [$V1] [VAL] [] }? ]
{ [ [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+ ]
  [ ['20\\d\\d' ? VAL: 'YEAR'->AVP]
    { [$V2]{5} [] }{2}
    { [$V2]{3} [] }?
  ]+
}+
```

- `$V1` = cell fragment; `[$V1]{4}` — four cells with PREFIX action, `[$V1]` — single cell.
- `$V2` = cell fragment; `[$V2]{5}` — five VALUE cells with REC, `[$V2]{3}` — three.
- Quantifiers on references are independent of the definition.

See `RtlFragmentTest` for unit tests covering all four fragment levels (cell, row, subrow, subtable).

```bash
mvn test -Dtest="RtlTask116Test"
mvn test -Dtest="RtlFragmentTest"
```

---

## Low-level ITM construction (without a pattern)

The ATP and RTL paths populate the semantic layer automatically by matching a pattern. For full
control — or for use cases the pattern language does not cover — an `InterpretableTable` can also
be assembled **by hand**: you create the cell-derived items, the context-derived items, and the
interpretation actions yourself, then interpret the result directly. No `AtpMatcher` is involved.

This example builds the simplified cross-tabulation (schema `⟨ND, AIRLINE, AIRPORT⟩`) from scratch:

```
       | CA | HU
IKT    |  5 |  3
SVO    | 31 | 40
```

```java
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.*;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.*;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import java.util.List;
import java.util.Set;

// 1. Build the syntactic layer
TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 0).setText("");
syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");
syntax.getCell(1, 1).setText("5");
syntax.getCell(1, 2).setText("3");
syntax.getCell(2, 0).setText("SVO");
syntax.getCell(2, 1).setText("31");
syntax.getCell(2, 2).setText("40");

// 2a. Cell-derived items (ι): one VALUE item per cell.
//     Index 0 = first (and only) item derived from that cell.
CellDerivedItem iotaCA  = new CellDerivedItem("CA",  0, syntax.getCell(0, 1), ItemType.VALUE);
CellDerivedItem iotaHU  = new CellDerivedItem("HU",  0, syntax.getCell(0, 2), ItemType.VALUE);
CellDerivedItem iotaIKT = new CellDerivedItem("IKT", 0, syntax.getCell(1, 0), ItemType.VALUE);
CellDerivedItem iotaSVO = new CellDerivedItem("SVO", 0, syntax.getCell(2, 0), ItemType.VALUE);
CellDerivedItem iota11  = new CellDerivedItem("5",   0, syntax.getCell(1, 1), ItemType.VALUE);
CellDerivedItem iota12  = new CellDerivedItem("3",   0, syntax.getCell(1, 2), ItemType.VALUE);
CellDerivedItem iota21  = new CellDerivedItem("31",  0, syntax.getCell(2, 1), ItemType.VALUE);
CellDerivedItem iota22  = new CellDerivedItem("40",  0, syntax.getCell(2, 2), ItemType.VALUE);

Set<CellDerivedItem> allCdi = Set.of(
        iotaCA, iotaHU, iotaIKT, iotaSVO, iota11, iota12, iota21, iota22);

// 2b. Context-derived items (β): named ATTRIBUTE constants that define the schema fields
ContextDerivedItem betaND      = new ContextDerivedItem("ND",      ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRLINE = new ContextDerivedItem("AIRLINE", ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRPORT = new ContextDerivedItem("AIRPORT", ItemType.ATTRIBUTE);
Set<ContextDerivedItem> allCtx = Set.of(betaND, betaAIRLINE, betaAIRPORT);

// 2c. Interpretation actions
// AVP: pair each VALUE item with its named ATTRIBUTE (establishes the schema field name).
// REC: anchor on each body cell; providers select the same-column airline and same-row airport.
ItemFilterCondition sameCol = (a, c) -> c.sameCol(a) && !c.sameCell(a);
ItemFilterCondition sameRow = (a, c) -> c.sameRow(a) && !c.sameCell(a);

List<InterpretationAction> actions = List.of(
        // AVP actions: bind header items to named attributes
        new InterpretationAction(iotaCA,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaHU,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaIKT,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        new InterpretationAction(iotaSVO,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        new InterpretationAction(iota11,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota12,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota21,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota22,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        // REC actions: form one record per body cell
        new InterpretationAction(iota11, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaCA
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaIKT
                new RecOperation()),
        new InterpretationAction(iota12, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaIKT
                new RecOperation()),
        new InterpretationAction(iota21, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaCA
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaSVO
                new RecOperation()),
        new InterpretationAction(iota22, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaSVO
                new RecOperation())
);

// 3. Build the semantic layer and interpret
TableSemantics semantics = new TableSemantics(allCdi, allCtx, actions);
InterpretableTable itm = new InterpretableTable(syntax, semantics);
Recordset result = new TableInterpreter().interpret(itm);
// schema ⟨ND, AIRLINE, AIRPORT⟩; four records:
// ⟨5, CA, IKT⟩  ⟨3, HU, IKT⟩  ⟨31, CA, SVO⟩  ⟨40, HU, SVO⟩
```

For cells that yield multiple items (e.g. `"0 Jan"` → `"0"` and `"Jan"`), create one
`CellDerivedItem` per item with distinct index values. See `CrosstabMinMaxTest` for a worked
example.

> This is the lowest-level entry point. In practice the [ATP](model/atp.md) and
> [RTL](rtl-reference.md) paths express the same result far more compactly — the entire block
> above collapses to the two-line RTL pattern shown in the
> [Getting started](getting-started.md) guide.

---

## Running all examples

```bash
# All ATP benchmark tests
mvn test -Dtest="AtpTask*Test"

# All RTL benchmark tests
mvn test -Dtest="RtlTask*Test"

# Illustrative example
mvn test -Dtest="AtpIllustrativeExampleTest"
```
