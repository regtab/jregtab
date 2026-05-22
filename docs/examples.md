# Examples

Three worked examples drawn from the Foofah benchmark and the paper's illustrative example.
For each task the ATP pattern and its RTL equivalent are shown side by side.

---

## Example 1 — Task 01: repeating subtables, flat record

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
mvn test -Dtest="AtpTask01Test"
mvn test -Dtest="RtlTask01Test"
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

## Example 3 — Task 02: cell match condition, string extraction, cardinality

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
  [ [BLANK?] [] ]? }+
```

### How it works

- `{2}` — the header row pattern repeats exactly twice.
- `VAL=NORM` — derive a value item with whitespace normalisation applied to the cell text.
- `[!BLANK ? VAL : …]` — the anchor cell pattern is guarded: only match non-blank cells.
- `(SC{2}, SR)->REC(2)`:
  - `SC{2}` — collect up to 2 items from the same subcolumn (the two header rows above).
  - `SR` — collect 1 item from the same subrow (the adjacent value cell).
  - `REC(2)` — the item at position 2 in the sequence (the second header) becomes the attribute name.
- `[ [BLANK?] [] ]?` — an optional footer row; the first cell must be blank (`BLANK?` is the match condition).

### Running the test

```bash
mvn test -Dtest="AtpTask02Test"
mvn test -Dtest="RtlTask02Test"
```

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
