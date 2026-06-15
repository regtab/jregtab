# jRegTab

**jRegTab** is an open-source Java library for pattern-driven data extraction from editable document tables.

Tables in spreadsheets, text documents, and web pages are designed for human readability, not machine processing. Cell meaning may depend on position, cells can be compound, headers can be hierarchical, and context may appear outside the table body. **RegTab** addresses this by letting you describe the table's regular structure as a *pattern*; a successful match yields a structured recordset automatically.

---

## How it works

```
TableSyntax  ──►  AtpMatcher.match(pattern, syntax)  ──►  TableInterpreter.interpret(itm)  ──►  Recordset
(grid of cells)       (structural matching)                 (semantic interpretation)           (records)
```

1. **Describe** the table structure as an **ATP** pattern — either in Java (`atp.spec.*`) or as a compact **RTL** string compiled with `RtlCompiler.compile()`.
2. **Match** the pattern against a `TableSyntax` grid with `AtpMatcher.match()`.
3. **Interpret** the result with `TableInterpreter` to get a `Recordset`.

---

## Quick example

```java
// Table:   Name  | Score
//          Alice | 95
//          Bob   | 87

TableSyntax syntax = new TableSyntax(3, 2);
syntax.getCell(0, 0).setText("Name");   syntax.getCell(0, 1).setText("Score");
syntax.getCell(1, 0).setText("Alice");  syntax.getCell(1, 1).setText("95");
syntax.getCell(2, 0).setText("Bob");    syntax.getCell(2, 1).setText("87");

TablePattern pattern = RtlCompiler.compile("""
    [ [ATTR]{2} ]
    [ [VAL : (^COL)->AVP, (SR)->REC]{2} ]+
    """);

Recordset rs = AtpMatcher.match(pattern, syntax)
    .map(itm -> new TableInterpreter().interpret(itm))
    .orElseThrow();

// rs.schema().attributes()  →  [Name, Score]
// rs.records().get(0)       →  {Name=Alice, Score=95}
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
