# Getting started

## Requirements

- Java 21 or later
- Maven 3.9+ (or Gradle)

## Installation

Add jRegTab to your project:

=== "Maven"

    ```xml
    <dependency>
        <groupId>ru.icc.regtab</groupId>
        <artifactId>regtab</artifactId>
        <version>0.3.0</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'ru.icc.regtab:regtab:0.3.0'
    ```

## Core concepts

jRegTab extracts structured records from a table in three steps:

1. **Describe the table structure** — write a pattern (either as Java objects or as an RTL string).
2. **Match** the pattern against the table — `AtpMatcher.match(pattern, syntax)`.
3. **Interpret** the match result — `TableInterpreter.interpret(itm)` returns a `Recordset`.

A pattern is an **Abstract Table Pattern (ATP)**. You can construct one in Java using the `atp.spec.*` builder API, or compile one from an **RTL** (Regular Table Language) string — a compact DSL designed for this purpose.

## First example

Consider a cross-tabulation of airline departures by airport:

```
        | CA     | HU
IKT     | 0 Jan  | 8 Feb
SVO     | 31 Jan | 40 Feb
```

The column headers are airlines (`CA`, `HU`), the row headers are airports (`IKT`, `SVO`), and each
body cell holds a compound `"ND MON"` value — a number of departures plus a month. The goal is to
*unpivot* this matrix into a flat recordset `⟨ND, AIRLINE, AIRPORT, MON⟩`:

```
ND | AIRLINE | AIRPORT | MON
0  | CA      | IKT     | Jan
8  | HU      | IKT     | Feb
31 | CA      | SVO     | Jan
40 | HU      | SVO     | Feb
```

### Step 1 — build the table

```java
import ru.icc.regtab.itm.syntax.TableSyntax;

TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 1).setText("CA");    syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");   syntax.getCell(1, 1).setText("0 Jan");   syntax.getCell(1, 2).setText("8 Feb");
syntax.getCell(2, 0).setText("SVO");   syntax.getCell(2, 1).setText("31 Jan");  syntax.getCell(2, 2).setText("40 Feb");
// The empty corner cell (0, 0) defaults to "".
```

### Step 2 — write the pattern

**Option A — RTL string** (recommended for readability):

```java
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.atp.spec.TablePattern;

TablePattern pattern = RtlCompiler.compile("""
    [ [] [VAL : 'AIRLINE'->AVP]+ ]
    [ [VAL : 'AIRPORT'->AVP]
      [VAL : (COL, ROW, CL)->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
    """);
```

- `[ [] [VAL : 'AIRLINE'->AVP]+ ]` — header subtable: skip the empty corner `[]`, then one-or-more
  column headers, each bound to the attribute `AIRLINE`.
- `[ [VAL : 'AIRPORT'->AVP] … ]+` — data subtable: one-or-more rows whose first cell is bound to `AIRPORT`.
- `[VAL : … " " VAL : 'MON'->AVP]` — the compound body cell is split at the space into two values:
  `ND` (the first segment) and `MON` (the second).
- `(COL, ROW, CL)->REC` — the `ND` value forms one record from the same-column `AIRLINE`,
  the same-row `AIRPORT`, and the same-cell `MON`.

**Option B — Java builder API** (full control):

```java
import ru.icc.regtab.atp.spec.*;

var sameCol  = ItemFilterConditionSpec.sameCol();
var sameRow  = ItemFilterConditionSpec.sameRow();
var sameCell = ItemFilterConditionSpec.sameCell();

// Compound body cell: "0 Jan" → ND ("0") + MON ("Jan")
var dataCell = CompoundContentSpec.of(
    AtomicContentSpec.val(
        ActionSpec.rec(
            ProviderSpec.val(1, sameCol),    // AIRLINE (column header)
            ProviderSpec.val(1, sameRow),    // AIRPORT (leftmost cell)
            ProviderSpec.val(1, sameCell)    // MON (same compound cell)
        ),
        ActionSpec.avp("ND")
    ),
    CompoundContentSpec.Segment.of(" ",
        AtomicContentSpec.val(ActionSpec.avp("MON"))
    )
);

TablePattern pattern = TablePattern.of(
    // Header subtable: skip the empty corner + one-or-more airline cells
    SubtablePattern.of(
        RowPattern.of(
            CellPattern.skip(),
            CellPattern.of(Quantifier.oneOrMore(),
                AtomicContentSpec.val(ActionSpec.avp("AIRLINE")))
        )
    ),
    // Data subtable: one-or-more rows of airport cell + one-or-more body cells
    SubtablePattern.of(
        RowPattern.of(Quantifier.oneOrMore(),
            CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
            CellPattern.of(Quantifier.oneOrMore(), dataCell)
        )
    )
);
```

### Step 3 — match and interpret

```java
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.recordset.Recordset;

import java.util.Optional;

Optional<InterpretableTable> match = AtpMatcher.match(pattern, syntax);
if (match.isEmpty()) {
    System.out.println("Pattern did not match.");
    return;
}

Recordset rs = new TableInterpreter()
    .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
    .interpret(match.get());

System.out.println(rs.schema().attributes()); // [ND, AIRLINE, AIRPORT, MON]
for (var record : rs.records()) {
    System.out.println(record.get("AIRPORT") + "/" + record.get("AIRLINE")
            + ": " + record.get("ND") + " (" + record.get("MON") + ")");
}
// IKT/CA: 0 (Jan)
// IKT/HU: 8 (Feb)
// SVO/CA: 31 (Jan)
// SVO/HU: 40 (Feb)
```

## What's next

- [Examples](examples.md) — three worked examples, including cross-row providers and compound cells.
- [RTL reference](rtl-reference.md) — complete syntax for the RTL DSL.
- [ITM](model/itm.md) — syntactic and semantic layers, working state, table interpretation.
- [ATP](model/atp.md) — pattern hierarchy, content specs, matching algorithm.
- [API reference](api.md) — public classes and methods.
