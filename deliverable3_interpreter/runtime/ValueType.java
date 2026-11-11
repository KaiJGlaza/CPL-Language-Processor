package runtime;

/** The two SCL types in our subset. */
public enum ValueType {
    INTEGER, REAL;

    /** "integer" -> INTEGER, "real" -> REAL. */
    public static ValueType fromKeyword(String kw) {
        return kw.equals("integer") ? INTEGER : REAL;
    }
}
