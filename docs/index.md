# jRegTab

**jRegTab** is an open-source Java library for pattern-driven data extraction from editable document tables.

Tables in spreadsheets, text documents, and web pages are designed for human readability, not machine processing. Cell meaning may depend on position, cells can be compound, headers can be hierarchical, and context may appear outside the table body. **RegTab** addresses this by letting you describe the table's regular structure as a *pattern*; a successful match yields a structured recordset automatically.

---

## How it works

```mermaid
flowchart TB
    A["TableSyntax<br/>(grid of cells)"]
    B["InterpretableTable<br/>(structural matching)"]
    C["Recordset<br/>(records)"]
    A -->|"AtpMatcher.match(pattern, syntax)"| B
    B -->|"TableInterpreter.interpret(itm)"| C
```

1. **Describe** the table structure as an **ATP** pattern — either in Java (`atp.spec.*`) or as a compact **RTL** string compiled with `RtlCompiler.compile()`.
2. **Match** the pattern against a `TableSyntax` grid with `AtpMatcher.match()`.
3. **Interpret** the result with `TableInterpreter` to get a `Recordset`.

---

## Quick example

```java
// Cross-tabulation:        | CA     | HU
//                  IKT     | 0 Jan  | 8 Feb
//                  SVO     | 31 Jan | 40 Feb

TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 1).setText("CA");    syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");   syntax.getCell(1, 1).setText("0 Jan");   syntax.getCell(1, 2).setText("8 Feb");
syntax.getCell(2, 0).setText("SVO");   syntax.getCell(2, 1).setText("31 Jan");  syntax.getCell(2, 2).setText("40 Feb");

// Unpivot into ⟨ND, AIRLINE, AIRPORT, MON⟩; each compound "ND MON" cell is split by space.
TablePattern pattern = RtlCompiler.compile("""
    [ [] [VAL : 'AIRLINE'->AVP]+ ]
    [ [VAL : 'AIRPORT'->AVP]
      [VAL : (COL, ROW, CL)->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
    """);

Recordset rs = AtpMatcher.match(pattern, syntax)
    .map(itm -> new TableInterpreter().interpret(itm))
    .orElseThrow();

// rs.schema().attributes()  →  [ND, AIRLINE, AIRPORT, MON]
// rs.records().get(0)       →  {ND=0, AIRLINE=CA, AIRPORT=IKT, MON=Jan}
```

The resulting recordset `rs`:

```
ND | AIRLINE | AIRPORT | MON
0  | CA      | IKT     | Jan
8  | HU      | IKT     | Feb
31 | CA      | SVO     | Jan
40 | HU      | SVO     | Feb
```

---

## Installation

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

Requires **Java 21+**.

---

## Features

- **ITM** (Interpretable Table Model) — formal syntactic and semantic representation of a table: *subtables → rows → subrows → cells*, with value, attribute, and auxiliary items.
- **ATP** (Abstract Table Pattern) — structural + interpretive pattern matching against an ITM instance.
- **RTL** (Regular Table Language) — compact DSL that compiles to ATP; dramatically reduces pattern verbosity.
- **ATP → RTL serializer** — round-trip: serialize any `TablePattern` back to an RTL string.
- **Content specs** — atomic, delimited, compound, and conditional cell content.
- **Action specs** — `REC`, `AVP`, `JOIN`, `FILL`, `PREFIX`, `SUFFIX` for rich schema construction.
- **Named fragments** — reuse recurring sub-patterns in RTL with `$name` definitions.
- **Post-processing** — whitespace normalization, field splitting, schema reordering.
- **150-task benchmark** — Foofah (50), RegTab (60), and Baikal (40) tasks, 1 500 test variants, 100 % pass rate.

---

## Documentation

| Section | Contents |
|---|---|
| [Getting started](getting-started.md) | Installation, first example, full pipeline walkthrough |
| [ITM](model/itm.md) | Syntactic and semantic layers, items, providers, working state, table interpretation |
| [ATP](model/atp.md) | Pattern hierarchy, quantifiers, content specs, action specs, matching |
| [RTL reference](rtl-reference.md) | Complete RTL syntax with tables and examples |
| [Examples](examples.md) | Three worked examples: repeating subtables, cross-row providers, compound cells |
| [Architecture](architecture.md) | Package map, data flow, compilation pipeline |
| [API reference](api.md) | All public classes, factories, and methods |
| [Benchmark](benchmark.md) | Foofah, RegTab, and Baikal task collections |
| [Testing](testing.md) | Test suite layout, fixtures, and how to run tasks |

---

!!! note "Status"
    Current release: **0.1.0** · License: **MIT** · [Maven Central](https://central.sonatype.com/artifact/ru.icc.regtab/regtab) · [GitHub](https://github.com/regtab/jregtab)
