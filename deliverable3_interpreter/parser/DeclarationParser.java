package parser;

import ast.Declaration;
import java.util.List;
import runtime.ValueType;

/** <declarations> ::= <declaration> { <declaration> }. Grammar is unchanged from D2. */
public class DeclarationParser {
    private final ParserContext ctx;

    public DeclarationParser(ParserContext ctx) { this.ctx = ctx; }

    /** One declaration, then more while the next token is "define". */
    public List<Declaration> declarations() {
        // TODO: loop calling declaration()
        throw new UnsupportedOperationException("declarations");
    }

    /** <declaration> ::= "define" IDENTIFIER "of" "type" <type> */
    private Declaration declaration() {
        // TODO: match the tokens in order; on any error synchronize at {define, begin} and return null
        // TODO: duplicate name -> ctx.parseError("duplicate declaration of 'x'", line); otherwise ctx.declare(name, type)
        throw new UnsupportedOperationException("declaration");
    }

    /** <type> ::= "integer" | "real". Package-private: Parser reads the function's return type with it too. */
    ValueType type() {
        // TODO: consume integer/real; otherwise ctx.error("type ('integer' or 'real')", current)
        throw new UnsupportedOperationException("type");
    }
}
