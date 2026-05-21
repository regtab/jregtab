package ru.icc.regtab.interpret;

/**
 * Missing value handler mu: A -> V | null.
 * Returns a default value for a given attribute, or null for missing.
 */
@FunctionalInterface
public interface MissingValueHandler {

    String handle(String attribute);

    /**
     * Default handler that returns null for all attributes.
     */
    MissingValueHandler NULL_HANDLER = attribute -> null;
}
