# IDE support

RTL patterns usually live inside Java string literals, which editors treat as plain text
by default. jregtab ships three layers of tooling that improve this:

1. **Syntax highlighting** — a TextMate grammar for `.rtl` files and for RTL embedded in
   Java strings (VS Code, IntelliJ, and any TextMate-compatible editor).
2. **Language-injection annotations** — `@Language("RTL")` on the `RtlCompiler.compile`
   parameters and the `@RtlSource` annotation for your own code (IntelliJ-based IDEs).
3. **Compile-time validation** — an annotation processor that turns an invalid RTL literal
   into a Java compilation error.

## Syntax highlighting

For VS Code there is a dedicated extension, [regtab/vscode-rtl](https://github.com/regtab/vscode-rtl).
The TextMate grammar also lives in this repository under
[`ide/`](https://github.com/regtab/jregtab/tree/main/ide): `ide/vscode/` serves as an
IntelliJ TextMate bundle (and works in any TextMate-compatible editor).

### VS Code

Install **[Regular Table Language (RTL)](https://marketplace.visualstudio.com/items?itemName=regtab.regtab)**
from the Marketplace — open the Extensions view (`Ctrl+Shift+X`), search for
*Regular Table Language*, or run:

```bash
ext install regtab.regtab
```

Beyond highlighting, the extension bundles a native language server (`rtl-lsp`): compile
diagnostics, live match preview against CSV fixtures, expected-result diffing, fragment
navigation, completion, and code snippets.

The extension highlights `*.rtl` files and RTL inside Java strings in these forms:

```java
TablePattern p = RtlCompiler.compile("""
        [ [ATTR] [VAL : (LT{1})->REC]+ ]+
        """);

String s = /* language=RTL */ "[ [ATTR] [VAL]+ ]";
```

!!! note "Limitation"
    TextMate matching is line-based: the opening quote must be on the same line as
    `RtlCompiler.compile(` or the `/* language=RTL */` marker comment.

### IntelliJ IDEA

*Settings → Editor → TextMate Bundles → “+”* and select the `ide/vscode` directory —
`*.rtl` files are highlighted. Injection into Java string literals is driven by the
annotations described next, where the IDE version supports injecting TextMate-backed
languages.

## Language-injection annotations

Both `RtlCompiler.compile(...)` overloads annotate their `rtl` parameter with
`@Language("RTL")` (`org.jetbrains:annotations`, a `provided`-scope dependency that is
**not** required at runtime and is not transitive).

For your own fields, parameters, or methods, use `@RtlSource` — it is meta-annotated with
`@Language("RTL")`, so IntelliJ treats the annotated string as RTL:

```java
import ru.icc.regtab.rtl.RtlSource;

@RtlSource
static final String PATTERN = """
        [ [ATTR] [VAL : (LT{1})->REC]+ ]+
        """;
```

## Compile-time validation

`RtlSourceProcessor` (registered via `META-INF/services`, shipped in the main jar)
validates every `@RtlSource`-annotated **compile-time `String` constant** during `javac`.
An invalid literal fails the Java build with the RTL error message attached to the field:

```
Sample.java:4: error: Invalid RTL: RTL compile error at 1:5: no viable alternative at input '[VAL'
```

Validation depth:

- literals containing `EXT(` are checked for **syntax only**, because full compilation
  requires the `Bindings` supplied at runtime;
- all other literals are fully compiled, so semantic errors (invalid provider templates,
  conflicting `REC(n)`/`REC('s')` parameters, …) are caught too.

Non-constant values — parameters, methods, non-`final` fields, and strings assembled at
runtime — are skipped silently.

### Enabling the processor

- **JDK 21–22**: runs automatically when regtab is on the compile classpath (javac prints
  a note about implicit annotation processing; silence it or opt in explicitly with
  `-proc:full`).
- **JDK 23+**: implicit annotation processing is disabled by default — enable it
  explicitly:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <proc>full</proc>
    </configuration>
</plugin>
```

To opt out entirely, pass `-proc:none`.
