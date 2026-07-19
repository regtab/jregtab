# IDE support for RTL

This directory ships editor tooling for RTL (Regular Table Language):

- a TextMate grammar for `.rtl` files (`vscode/syntaxes/rtl.tmLanguage.json`),
- an injection grammar that highlights RTL inside Java string literals
  (`vscode/syntaxes/rtl-java-injection.tmLanguage.json`),
- a VS Code extension manifest wiring both together (`vscode/`).

The same `vscode/` directory doubles as a TextMate bundle for IntelliJ-based IDEs.

## VS Code

Use the dedicated extension [regtab/vscode-rtl](https://github.com/regtab/vscode-rtl) —
download the VSIX for your platform from its
[Releases](https://github.com/regtab/vscode-rtl/releases) and install via
`code --install-extension <file>.vsix` or *Extensions → … → Install from VSIX*.
Besides highlighting it bundles a native language server (`rtl-lsp`) with compile
diagnostics, live match preview, and more.

The `vscode/` directory here remains a minimal highlighting-only bundle; you can still
copy it into your extensions directory as a fallback
(`cp -r ide/vscode ~/.vscode/extensions/regtab.rtl-language-0.1.0`), which gives you:

- syntax highlighting for `*.rtl` files;
- highlighting of RTL inside Java strings for these forms:
  - `RtlCompiler.compile("""…""")` and `RtlCompiler.compile("…")`,
  - any string literal preceded by the marker comment `/* language=RTL */` on the same line.

Limitation (inherent to TextMate): the opening quote must be on the same line as
`compile(` or the marker comment.

## IntelliJ IDEA (and other JetBrains IDEs)

1. *Settings → Editor → TextMate Bundles → “+”* and select the `ide/vscode` directory.
2. `*.rtl` files are now highlighted.

For RTL inside Java string literals, jregtab's API is annotated with
`@Language("RTL")` (`org.jetbrains:annotations`), and the `@ru.icc.regtab.rtl.RtlSource`
annotation is available for your own fields and parameters. When the IDE knows the
RTL language (registered TextMate bundle or a dedicated plugin), these annotations let
IntelliJ inject RTL into the literals; support for injecting TextMate-backed languages
varies by IDE version — where unavailable, you still get `.rtl` file highlighting.

## Keeping the grammar in sync

The TextMate grammar mirrors the tokens of `src/main/antlr4/ru/icc/regtab/rtl/RTL.g4`.
Any change to `RTL.g4` must be accompanied by a matching update to
`vscode/syntaxes/rtl.tmLanguage.json` in the same PR.
