package scanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of D1's scl_scanner.py. Pipeline:
 *   source text -> removeComments -> splitLines -> scanLine (per line) -> tokens
 *
 * Named SclScanner (not Scanner) so it does not collide with java.util.Scanner.
 * Error policy (same as D1): report, skip the bad piece, keep scanning.
 */
public class SclScanner {
    private final List<String> errors = new ArrayList<>();
    private int col;   // current column while scanning one line; the read* methods advance it

    /** Scan a whole source file's text and return its tokens. */
    public List<Token> scan(String source) {
        // TODO: cleaned = removeComments(source)
        // TODO: for each line from splitLines(cleaned) (index + 1 = line number): tokens.addAll(scanLine(...))
        // TODO: must reproduce D1 token counts (test_input.scl = 99, empty file = 0)
        throw new UnsupportedOperationException("SclScanner.scan");
    }

    /** Errors found while scanning, each as "Line N: message". */
    public List<String> errors() { return errors; }

    /** Record and print one scanning error. */
    private void reportError(int line, String message) {
        // TODO: add "Line N: message" to errors and print it
        throw new UnsupportedOperationException("reportError");
    }

    /**
     * Strip comments ("//" to end of line; "description" up to and including the closing star-slash).
     * Keep newlines so line numbers stay correct. Do not treat comment markers inside strings as comments.
     * "description" counts only as a whole word. Unterminated -> reportError at the line it started.
     */
    private String removeComments(String text) {
        // TODO: port remove_comments from D1
        throw new UnsupportedOperationException("removeComments");
    }

    /** Split on '\n' only; strip trailing '\r'; keep blank lines; list index + 1 = line number. */
    private List<String> splitLines(String text) {
        // TODO: port split_lines from D1 (no extra empty line for a final newline)
        throw new UnsupportedOperationException("splitLines");
    }

    /** Scan one line: whitespace -> skip; letter -> readWord; digit -> readNumber; '"' -> readString; operator char -> readOperator; else unknown character. */
    private List<Token> scanLine(String line, int lineNumber) {
        // TODO: port scan_line from D1 (use the col field; unknown char -> reportError + UNKNOWN token)
        throw new UnsupportedOperationException("scanLine");
    }

    /** letter { letter | digit | '_' } -> KEYWORD (lowercase only) or IDENTIFIER. */
    private Token readWord(String line, int lineNumber) {
        // TODO: read the word starting at col, advance col, look up TokenTable.KEYWORDS
        throw new UnsupportedOperationException("readWord");
    }

    /** digit {digit} ['.' digit {digit}] -> INTEGER_CONST or REAL_CONST. Errors: '3.' malformed real; '2count' invalid token. Return null on error. */
    private Token readNumber(String line, int lineNumber) {
        // TODO: port read_number from D1
        throw new UnsupportedOperationException("readNumber");
    }

    /** Opening quote to closing quote; token value = text between. No closing quote on the line -> "unterminated string", return null. */
    private Token readString(String line, int lineNumber) {
        // TODO: port read_string from D1
        throw new UnsupportedOperationException("readString");
    }

    /** Operators, with '==' (108) winning over '=' (101). */
    private Token readOperator(String line, int lineNumber) {
        // TODO: port read_operator from D1 (look ahead one char)
        throw new UnsupportedOperationException("readOperator");
    }

    private static boolean isLetter(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isWordChar(char c) { return isLetter(c) || isDigit(c) || c == '_'; }
}
