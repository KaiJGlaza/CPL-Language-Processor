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
 * Error format: Syntax error (line N): expected X, found Y
 */
public class ParserContext {
    private final List<Token> tokens;
    private int pos = 0;
    private final Map<String, ValueType> declared = new HashMap<>();
    private final List<String> errors = new ArrayList<>();

    public ParserContext(List<Token> tokens) { this.tokens = tokens; }

    /** Peek at the current token, or null at end of input. */
    public Token current() { return pos < tokens.size() ? tokens.get(pos) : null; }

    /** Consume and return the current token, or null at end of input. */
    public Token consume() { return pos < tokens.size() ? tokens.get(pos++) : null; }

    /** Required public function: next token (comments never reach the parser). */
    public Token getNextToken() { return consume(); }

    // ---- declared names (the parser's own table, not the scanner's) ----
    public boolean identifierExists(String name) { return declared.containsKey(name); }
    public void declare(String name, ValueType type) { declared.put(name, type); }
    public ValueType typeOf(String name) { return declared.get(name); }

    // ---- errors ----
    public List<String> errors() { return errors; }
    public int errorCount() { return errors.size(); }

    /** Consume the current token if it is the keyword or operator `expected` (e.g. "then", "="); else error() and return null. */
    public Token match(String expected) {
        // TODO: check type KEYWORD or OPERATOR and value equals expected; consume on success
        // TODO: on failure call error("'" + expected + "'", current()) and return null
        throw new UnsupportedOperationException("ParserContext.match");
    }

    /** Same, for token types IDENTIFIER or STRING. */
    public Token matchType(TokenType expected) {
        // TODO: label is "identifier" or "string literal"
        throw new UnsupportedOperationException("ParserContext.matchType");
    }

    /** Record and print: Syntax error (line N): expected X, found Y. found == null means end of file. */
    public void error(String expected, Token found) {
        // TODO: line = found's line (or the last token's line at end of file); describe found as 'value' or "end of file"
        throw new UnsupportedOperationException("ParserContext.error");
    }

    /** Record and print an error with a free-form message (duplicate or undeclared variable, etc.). */
    public void parseError(String message, int line) {
        // TODO: same "Syntax error (line N): message" format
        throw new UnsupportedOperationException("ParserContext.parseError");
    }

    /** After an error, skip tokens until one whose value is in stopAt (not consumed). */
    public void synchronize(Set<String> stopAt) {
        // TODO: statement recovery stops at: set display input return exit if else endif endfun
        // TODO: declaration recovery stops at: define begin
        throw new UnsupportedOperationException("ParserContext.synchronize");
    }
}
