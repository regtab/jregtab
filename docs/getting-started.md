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
        <version>0.1.0</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'ru.icc.regtab:regtab:0.1.0'
    ```

## Core concepts

jRegTab extracts structured records from a table in three steps:

1. **Describe the table structure** — write a pattern (either as Java objects or as an RTL string).
2. **Match** the pattern against the table — `AtpMatcher.match(pattern, syntax)`.
3. **Interpret** the match result — `TableInterpreter.interpret(itm)` returns a `Recordset`.

A pattern is an **Abstract Table Pattern (ATP)**. You can construct one in Java using the `atp.spec.*` builder API, or compile one from an **RTL** (Regular Table Language) string — a compact DSL designed for this purpose.

## First example

Consider a simple two-column table:

```
Name   | Score
Alice  | 95
Bob    | 87
```

The first row contains attribute names; the remaining rows contain values.

### Step 1 — build the table

```java
import ru.icc.regtab.itm.syntax.TableSyntax;

TableSyntax syntax = new TableSyntax(3, 2);
syntax.getCell(0, 0).setText("Name");  syntax.getCell(0, 1).setText("Score");
syntax.getCell(1, 0).setText("Alice"); syntax.getCell(1, 1).setText("95");
syntax.getCell(2, 0).setText("Bob");   syntax.getCell(2, 1).setText("87");
```

### Step 2 — write the pattern

**Option A — RTL string** (recommended for readability):

```java
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.atp.spec.TablePattern;

TablePattern pattern = RtlCompiler.compile("""
    [ [ATTR]{2} ]
    [ [VAL : (^COL)->AVP, (SR)->REC]{2} ]+
    """);
```

- `[ [ATTR]{2} ]` — one header row with exactly two attribute cells.
- `[ … ]+` — one or more data rows.
- `(^COL)->AVP` — link each value to the attribute in the same column (header row above).
- `(SR)->REC` — collect all values in the same row into one record.

**Option B — Java builder API** (full control):

```java
import ru.icc.regtab.atp.spec.*;

var avpProvider = ProviderSpec.attr(ItemFilterConditionSpec.sameCol());
var recProvider = ProviderSpec.val(ItemFilterConditionSpec.sameRow());

TablePattern pattern = TablePattern.of(
    // Header subtable: two attribute cells
    SubtablePattern.of(
        RowPattern.of(
            CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.attr())
        )
    ),
    // Data subtable: one or more rows with two value cells each
    SubtablePattern.of(
        RowPattern.of(Quantifier.oneOrMore(),
            CellPattern.of(AtomicContentSpec.val(
                ActionSpec.avp(avpProvider),
                ActionSpec.rec(recProvider)
            )),
            CellPattern.of(AtomicContentSpec.val(
                ActionSpec.avp(avpProvider)
            ))
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

System.out.println(rs.schema().attributes()); // [Name, Score]
for (var record : rs.records()) {
    System.out.println(record.get("Name") + " → " + record.get("Score"));
}
// Alice → 95
// Bob  → 87
```

## What's next

- [Examples](examples.md) — three worked examples, including cross-row providers and compound cells.
- [RTL reference](rtl-reference.md) — complete syntax for the RTL DSL.
- [ITM](model/itm.md) — syntactic and semantic layers, working state, table interpretation.
- [ATP](model/atp.md) — pattern hierarchy, content specs, matching algorithm.
- [API reference](api.md) — public classes and methods.
