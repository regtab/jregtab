package ru.icc.regtab.rtl;

/** Thrown when an RTL string fails to parse or compile into a TablePattern. */
public final class RtlCompileException extends RuntimeException {

    private final int line;
    private final int column;

    public RtlCompileException(String message, int line, int column) {
        super("RTL compile error at " + line + ":" + column + ": " + message);
        this.line = line;
        this.column = column;
    }

    public RtlCompileException(String message) {
        this(message, -1, -1);
    }

    public int line()   { return line; }
    public int column() { return column; }
}
