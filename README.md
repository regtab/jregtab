<p align="center">
  <img src="assets/icon.svg" alt="jRegTab" width="100"/>
</p>

# jRegTab

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/ru.icc.regtab/regtab.svg)](https://central.sonatype.com/artifact/ru.icc.regtab/regtab)
[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://www.oracle.com/java/)

**jRegTab** is an open-source Java library implementing **RegTab** — a method for pattern-driven data extraction from editable document tables with regular structure.

<!-- RegTab is described in the paper:

> Igor V. Bychkov, Alexey E. Hmelnov, and Alexey O. Shigarov.
> *RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure.*
> Submitted to IEEE Transactions on Knowledge and Data Engineering. -->

---

## Overview

Tabular data in spreadsheets, text documents, and web pages are among the most common sources for data analysis. Extracting structured records from such tables is a critical but labour-intensive step in data wrangling. Source tables are typically designed for human readability and lack explicit semantics: cell meaning may be independent of position, cells may be compound, headers may be hierarchical, and relevant context may appear outside the table itself.

RegTab addresses this by matching editable document tables against *patterns* that capture their regular structure and interpretive logic. A successful match enriches the table with semantic information and yields a structured recordset.

The method is built around two formal models:

- **Interpretable Table Model (ITM)** — represents the syntactic and semantic structure of a table. The syntactic layer describes cells (their positions, formatting, and text content) together with a row-oriented substructure hierarchy: *subtables → rows → subrows → cells*. The semantic layer consists of *items* (value-associated, attribute-associated, and auxiliary) derived from cell content or supplied from external context, along with *interpretation actions* that establish how items form attribute–value pairs and record item sequences.

- **Abstract Table Pattern (ATP)** — specifies a class of tables and the rules for deriving structured records from them. An ATP instance mirrors the ITM hierarchy and contains *cell patterns* with *cell match conditions*, *content specifications*, and *interpretation action specifications*. Matching an ATP against an ITM instance populates the semantic layer automatically.

Patterns can be written directly with the Java fluent API or, more compactly, in **RTL** (Regular Table Language) — a textual DSL that compiles to ATP.

---

## Quick start

```java
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.rtl.RtlCompiler;

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

A step-by-step walkthrough of this example (including the equivalent Java fluent API and a
low-level ITM construction) is in the [Getting started](docs/getting-started.md) guide.

---

## Documentation

The full documentation site is published at <https://regtab.github.io/jregtab/>.

- [Getting started](docs/getting-started.md) — installation, first example, full pipeline walkthrough
- [ITM](docs/model/itm.md) — syntactic and semantic layers, items, providers, working state, interpretation
- [ATP](docs/model/atp.md) — pattern hierarchy, content specs, action specs, matching
- [RTL reference](docs/rtl-reference.md) — complete RTL syntax with tables and examples
- [Examples](docs/examples.md) — worked examples with ATP and RTL patterns side by side
- [Architecture](docs/architecture.md) — package map, data flow, RTL compilation pipeline
- [API reference](docs/api.md) — public classes, factories, and methods (full Javadoc on [javadoc.io](https://javadoc.io/doc/ru.icc.regtab/regtab))
- [Benchmark](docs/benchmark.md) — Foofah, RegTab, and Baikal task collections
- [Testing](docs/testing.md) — test suite layout, fixtures, and how to run tasks

For local preview, run `serve.bat` (Windows) or on any OS:

```bash
pip install -r requirements.txt
mkdocs serve
```

Then open <http://127.0.0.1:8000>. Publishing is automated by the `Deploy docs` GitHub Actions workflow on every push to `main`.

---

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>ru.icc.regtab</groupId>
    <artifactId>regtab</artifactId>
    <version>0.1.0</version>
</dependency>
```

Requires **Java 21+** and **Maven 3.9+**.

---

## Build

```bash
mvn compile      # compile
mvn test         # compile and run the full test suite
```

The library is evaluated on **150 benchmark tasks** (Foofah, RegTab, and Baikal collections),
covering **1 500 test variants (750 ATP + 750 RTL)** with 100 % accuracy. See
[Benchmark](docs/benchmark.md) and [Testing](docs/testing.md) for details.

---

## Related work

jRegTab builds on and supersedes **TabbyXL** (<https://github.com/tabbydoc/tabbyxl>), an earlier platform for tabular-data understanding based on the CRL domain-specific language.

<!-- ---

## Citation

If you use jRegTab in your research, please cite:

```
Igor V. Bychkov, Alexey E. Hmelnov, and Alexey O. Shigarov.
RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure.
Submitted to IEEE Transactions on Knowledge and Data Engineering, 2025.
``` -->

---

## License

This project is distributed under the terms of the MIT License. See [LICENSE](LICENSE) for details.
