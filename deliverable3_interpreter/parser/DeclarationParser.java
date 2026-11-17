package parser;

import ast.Declaration;
import java.util.ArrayList;
import java.util.List;
import runtime.ValueType;
import scanner.Token;
import scanner.TokenType;

/** <declarations> ::= <declaration> { <declaration> }. Grammar is unchanged from D2. */
public class DeclarationParser {
    private final ParserContext ctx;

    public DeclarationParser(ParserContext ctx) { this.ctx = ctx; }

    /** One declaration, then more while the next token is "define". */
    public List<Declaration> declarations() {
        List<Declaration> result = new ArrayList<>();

        Declaration first = declaration();
        if (first != null) result.add(first);

        while (isKeyword("define")) {
            Declaration d = declaration();   // null when the define had an error
            if (d != null) result.add(d);
        }
        return result;
    }

    /** <declaration> ::= "define" IDENTIFIER "of" "type" <type> */
    private Declaration declaration() {
        int errorsBefore = ctx.errorCount();

        // Match in order; && stops at the first token that does not fit.
        Token name = null;
        ValueType type = null;
        if (ctx.match("define") != null) {
            name = ctx.matchType(TokenType.IDENTIFIER);
        }
        if (name != null && ctx.match("of") != null && ctx.match("type") != null) {
            type = type();
        }

        // Any syntax error: skip the rest of this define so it gives ONE error, not a chain.
        if (ctx.errorCount() > errorsBefore) {
            ctx.synchronize(ParserContext.DECLARATION_SYNC);
            return null;
        }

        if (ctx.identifierExists(name.value)) {
            ctx.parseError("duplicate declaration of '" + name.value + "'", name.line);
            return null;
        }
        ctx.declare(name.value, type);
        return new Declaration(name.value, type, name.line);
    }

    /** <type> ::= "integer" | "real". Package-private: Parser reads the function's return type with it too. */
    ValueType type() {
        Token tok = ctx.current();
        if (tok != null && tok.type == TokenType.KEYWORD
                && (tok.value.equals("integer") || tok.value.equals("real"))) {
            ctx.consume();
            return ValueType.fromKeyword(tok.value);
        }
        ctx.error("type ('integer' or 'real')", tok);
        return null;   // caller sees the error; type() never touches the declared-names table
    }

    private boolean isKeyword(String kw) {
        Token t = ctx.current();
        return t != null && t.type == TokenType.KEYWORD && t.value.equals(kw);
    }
}