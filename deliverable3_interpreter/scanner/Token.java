package scanner;

/** One scanned token: type, ID number, text, and source line. (Done: contract only.) */
public class Token {
    public final TokenType type;
    public final int id;
    public final String value;   // for STRING: the text between the quotes
    public final int line;

    public Token(TokenType type, int id, String value, int line) {
        this.type = type;
        this.id = id;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Line " + line + ": " + type + " (id " + id + ") -> " + value;
    }
}
