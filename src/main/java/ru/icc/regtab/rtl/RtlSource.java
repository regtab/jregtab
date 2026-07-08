package ru.icc.regtab.rtl;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code String} as RTL (Regular Table Language) source.
 *
 * <p>Serves two purposes:
 * <ul>
 *   <li><b>IDE language injection</b> — the annotation is meta-annotated with
 *       {@code @Language("RTL")}, so IntelliJ-based IDEs that know the RTL language
 *       inject RTL highlighting into the annotated literal.</li>
 *   <li><b>Compile-time validation</b> — {@link ru.icc.regtab.rtl.processor.RtlSourceProcessor}
 *       validates annotated compile-time {@code String} constants during {@code javac}:
 *       an invalid RTL literal becomes a Java compilation error. Non-constant values
 *       (parameters, methods, non-final fields) are skipped silently.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @RtlSource
 * static final String PATTERN = """
 *     [ [SKIP] [VAL : ('AIRLINE')->AVP]+ ]
 *     """;
 * }</pre>
 */
@Documented
@Language("RTL")
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface RtlSource {
}
