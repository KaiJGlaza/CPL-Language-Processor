package scanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of D1's scl_scanner.py. Pipeline:
 *   source text -> removeComments -> splitLines -> scanLine (per line) -> tokens
 */
public class SclScanner {
    private final List<String> errors = new ArrayList<>();
    private int col;   // current column while scanning one line; the read* methods advance it

    /** Characters that can begin an operator ('=' starts both '=' and '=='). */
    private static final String OPERATOR_START_CHARS = "=+-*/><,";


    public List<Token> scan(String source) {
        errors.clear();                                   // a scanner object can be reused
        String cleaned = removeComments(source);
        List<Token> tokens = new ArrayList<>();
        List<String> lines = splitLines(cleaned);
        for (int i = 0; i < lines.size(); i++) {
            tokens.addAll(scanLine(lines.get(i), i + 1)); // index + 1 = line number
        }
        return tokens;
    }

    /** Errors found while scanning, each as "Line N: message". */
    public List<String> errors() { return errors; }

    /** Record and print one scanning error. */
    private void reportError(int line, String message) {
        errors.add("Line " + line + ": " + message);
        System.out.println("Line " + line + ": ERROR - " + message);
    }

    /**
     * Strip comments ("//" to end of line; "description" up to and including the closing star-slash).
     * Keep newlines so line numbers stay correct.
     */
    private String removeComments(String text) {
        StringBuilder result = new StringBuilder();
        int n = text.length();
        int i = 0;
        int line = 1;
        boolean inString = false;

        while (i < n) {
            char ch = text.charAt(i);

            if (inString) {
                result.append(ch);
                if (ch == '"') {
                    inString = false;
                } else if (ch == '\n') {
                    inString = false;
                    line++;
                }
                i++;
                continue;
            }

            // Start of a string.
            if (ch == '"') {
                inString = true;
                result.append(ch);
                i++;
                continue;
            }

            // '//' comment: skip to the end of the line (keep the newline).
            if (text.startsWith("//", i)) {
                while (i < n && text.charAt(i) != '\n') i++;
                result.append(' ');
                continue;
            }

            // 'description' comment: only when it is a whole word.
            if (text.startsWith("description", i)) {
                int after = i + "description".length();
                boolean beforeOk = i == 0 || !isWordChar(text.charAt(i - 1));
                boolean afterOk = after >= n || !isWordChar(text.charAt(after));
                if (beforeOk && afterOk) {
                    int close = text.indexOf("*/", after);
                    if (close == -1) {
                        reportError(line, "unterminated comment (started with 'description')");
                        close = n;
                    } else {
                        close += 2;
                    }
                    int skippedNewlines = 0;
                    for (int k = i; k < close; k++) if (text.charAt(k) == '\n') skippedNewlines++;
                    result.append(' ');
                    for (int k = 0; k < skippedNewlines; k++) result.append('\n');
                    line += skippedNewlines;
                    i = close;
                    continue;
                }
            }

            // Ordinary character.
            if (ch == '\n') line++;
            result.append(ch);
            i++;
        }
        return result.toString();
    }

    /** Split on '\n' only; strip trailing '\r'; keep blank lines; list index + 1 = line number. */
    private List<String> splitLines(String text) {
        // limit -1 keeps trailing empty strings, like Python's split("\n")
        String[] pieces = text.split("\n", -1);
        int count = pieces.length;
        if (count > 0 && pieces[count - 1].isEmpty()) count--;
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String p = pieces[i];
            int end = p.length();
            while (end > 0 && p.charAt(end - 1) == '\r') end--;
            lines.add(p.substring(0, end));
        }
        return lines;
    }

    /** Scan one line: whitespace -> skip; letter -> readWord; digit -> readNumber; '"' -> readString; operator char -> readOperator; else unknown character. */
    private List<Token> scanLine(String line, int lineNumber) {
        List<Token> tokens = new ArrayList<>();
        col = 0;
        int n = line.length();

        while (col < n) {
            int cp = line.codePointAt(col);   // code point, so an emoji is ONE unknown character
            char ch = line.charAt(col);

            if (isSpace(cp)) {
                col += Character.charCount(cp);
                continue;
            }

            Token token;
            int start = col;
            if (isLetter(ch)) {
                token = readWord(line, lineNumber);
            } else if (isDigit(ch)) {
                token = readNumber(line, lineNumber);
            } else if (ch == '"') {
                token = readString(line, lineNumber);
            } else if (OPERATOR_START_CHARS.indexOf(ch) >= 0) {
                token = readOperator(line, lineNumber);
            } else {
                String bad = new String(Character.toChars(cp));
                reportError(lineNumber, "unknown character '" + bad + "'");
                token = new Token(TokenType.UNKNOWN, TokenTable.UNKNOWN_ID, bad, lineNumber);
                col += Character.charCount(cp);
            }

            if (token != null) tokens.add(token);
            if (col <= start) col = start + 1;   // safety: always make progress
        }
        return tokens;
    }

    /** letter { letter | digit | '_' } -> KEYWORD (lowercase only) or IDENTIFIER. */
    private Token readWord(String line, int lineNumber) {
        int end = col + 1;
        while (end < line.length() && isWordChar(line.charAt(end))) end++;
        String word = line.substring(col, end);
        col = end;

        Integer kwId = TokenTable.KEYWORDS.get(word);
        if (kwId != null) return new Token(TokenType.KEYWORD, kwId, word, lineNumber);
        return new Token(TokenType.IDENTIFIER, TokenTable.IDENTIFIER_ID, word, lineNumber);
    }

    /** digit {digit} ['.' digit {digit}] -> INTEGER_CONST or REAL_CONST. Errors: '3.' malformed real; '2count' invalid token. Return null on error. */
    private Token readNumber(String line, int lineNumber) {
        int n = line.length();
        int start = col;
        int end = start;
        while (end < n && isDigit(line.charAt(end))) end++;

        boolean isReal = false;
        if (end < n && line.charAt(end) == '.') {
            if (end + 1 < n && isDigit(line.charAt(end + 1))) {
                end++;
                while (end < n && isDigit(line.charAt(end))) end++;
                isReal = true;
            } else {
                reportError(lineNumber, "malformed real constant '" + line.substring(start, end + 1)
                        + "' (digits required after '.')");
                col = end + 1;
                return null;
            }
        }

        if (end < n && isWordChar(line.charAt(end))) {
            while (end < n && isWordChar(line.charAt(end))) end++;
            reportError(lineNumber, "invalid token '" + line.substring(start, end)
                    + "' (identifiers must start with a letter)");
            col = end;
            return null;
        }

        String text = line.substring(start, end);
        col = end;
        return isReal
                ? new Token(TokenType.REAL_CONST, TokenTable.REAL_CONST_ID, text, lineNumber)
                : new Token(TokenType.INTEGER_CONST, TokenTable.INTEGER_CONST_ID, text, lineNumber);
    }

    /** Opening quote to closing quote; token value = text between. No closing quote on the line -> "unterminated string", return null. */
    private Token readString(String line, int lineNumber) {
        int close = line.indexOf('"', col + 1);
        if (close == -1) {
            reportError(lineNumber, "unterminated string");
            col = line.length();
            return null;
        }
        String value = line.substring(col + 1, close);
        col = close + 1;
        return new Token(TokenType.STRING, TokenTable.STRING_ID, value, lineNumber);
    }

    /** Operators, with '==' (108) winning over '=' (101). */
    private Token readOperator(String line, int lineNumber) {
        if (col + 2 <= line.length()) {
            String two = line.substring(col, col + 2);
            Integer id = TokenTable.OPERATORS.get(two);
            if (id != null) {
                col += 2;
                return new Token(TokenType.OPERATOR, id, two, lineNumber);
            }
        }
        String one = line.substring(col, col + 1);
        Integer id = TokenTable.OPERATORS.get(one);
        if (id != null) {
            col += 1;
            return new Token(TokenType.OPERATOR, id, one, lineNumber);
        }
        return null;   // not reached: scanLine only calls this for operator start characters
    }

    private static boolean isLetter(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isWordChar(char c) { return isLetter(c) || isDigit(c) || c == '_'; }
    private static boolean isSpace(int cp) { return Character.isWhitespace(cp) || Character.isSpaceChar(cp); }
}