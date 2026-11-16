package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import runtime.ValueType;
import scanner.Token;
import scanner.TokenType;

/**
 * State shared by every parser class: the tokens, the position, the declared names, and the errors.
 */
public class ParserContext {
    private final List<Token> tokens;
    private int pos = 0;
    private final Map<String, ValueType> declared = new HashMap<>();
    private final List<String> errors = new ArrayList<>();

    public ParserContext(List<Token> tokens) { this.tokens = tokens; }


    public Token current() { return pos < tokens.size() ? tokens.get(pos) : null; }


    public Token consume() { return pos < tokens.size() ? tokens.get(pos++) : null; }


    public Token getNextToken() { return consume(); }

    // ---- declared names (the parser's own table, not the scanner's) ----
    public boolean identifierExists(String name) { return declared.containsKey(name); }
    public void declare(String name, ValueType type) { declared.put(name, type); }
    public ValueType typeOf(String name) { return declared.get(name); }

    // ---- errors ----
    public List<String> errors() { return errors; }
    public int errorCount() { return errors.size(); }

    /** Statement recovery stops at the next statement keyword or block terminator. */
    public static final Set<String> STATEMENT_SYNC = Set.of(
            "set", "display", "input", "return", "exit", "if", "else", "endif", "endfun");

    /** Declaration recovery stops at the next define or at begin. */
    public static final Set<String> DECLARATION_SYNC = Set.of("define", "begin");

    /** Consume the current token if it is the keyword or operator `expected` (e.g. "then", "="); else error() and return null. */
    public Token match(String expected) {
        Token tok = current();
        // Check the TYPE too: a string literal whose text is "then" must not match the keyword.
        boolean fits = tok != null
                && (tok.type == TokenType.KEYWORD || tok.type == TokenType.OPERATOR)
                && tok.value.equals(expected);
        if (fits) return consume();
        error("'" + expected + "'", tok);
        return null;
    }

    /** Same, for token types IDENTIFIER or STRING. */
    public Token matchType(TokenType expected) {
        String label;
        if (expected == TokenType.IDENTIFIER) label = "identifier";
        else if (expected == TokenType.STRING) label = "string literal";
        else throw new IllegalArgumentException("matchType: unsupported token type " + expected);

        Token tok = current();
        if (tok != null && tok.type == expected) return consume();
        error(label, tok);
        return null;
    }

    /** Record and print: Syntax error (line N): expected X, found Y. found == null means end of file. */
    public void error(String expected, Token found) {
        String foundText = (found == null) ? "end of file" : "'" + found.value + "'";
        parseError("expected " + expected + ", found " + foundText, found != null ? found.line : -1);
    }

    /** Record and print an error with a free-form message (duplicate or undeclared variable, etc.). */
    public void parseError(String message, int line) {
        // No usable line (end of file): fall back to the last token's line, as in D2.
        if (line <= 0 && !tokens.isEmpty()) line = tokens.get(tokens.size() - 1).line;
        String loc = line > 0 ? " (line " + line + ")" : "";
        String full = "Syntax error" + loc + ": " + message;
        errors.add(full);
        System.out.println(full);
    }

    /** After an error, skip tokens until a KEYWORD whose value is in stopAt (not consumed). */
    public void synchronize(Set<String> stopAt) {
        while (true) {
            Token tok = current();
            if (tok == null) return;
            if (tok.type == TokenType.KEYWORD && stopAt.contains(tok.value)) return;
            consume();
        }
    }
}