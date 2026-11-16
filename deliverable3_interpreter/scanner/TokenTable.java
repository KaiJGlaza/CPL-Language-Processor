package scanner;

import java.util.LinkedHashMap;
import java.util.Map;

/** Keyword and operator tables. IDs must match D1's scl_token.py. */
public final class TokenTable {
    private TokenTable() {}

    public static final Map<String, Integer> KEYWORDS = new LinkedHashMap<>();
    public static final Map<String, Integer> OPERATORS = new LinkedHashMap<>();

    public static final int IDENTIFIER_ID = 200;
    public static final int INTEGER_CONST_ID = 300;
    public static final int REAL_CONST_ID = 301;
    public static final int STRING_ID = 400;
    public static final int UNKNOWN_ID = 999;

    static {
        String[] kw = {
            "import", "implementations", "function", "main", "return", "type",
            "is", "variables", "begin", "endfun",
            "define", "of", "integer", "real",
            "set", "display", "input", "exit",
            "if", "then", "else", "endif"
        };
        for (int i = 0; i < kw.length; i++) KEYWORDS.put(kw[i], i + 1);   // IDs 1-22

        String[] ops = { "=", "+", "-", "*", "/", ">", "<", "==", "," };
        for (int i = 0; i < ops.length; i++) OPERATORS.put(ops[i], 101 + i); // IDs 101-109
    }
}
