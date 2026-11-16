package parser;

import ast.Condition;
import ast.Expr;

/**
 * Expressions and conditions. Unlike D2 (which returned strings for logging), these build Expr nodes.
 * + and - bind looser than * and /; both are left-associative.
 */
public class ExpressionParser {
    private final ParserContext ctx;

    public ExpressionParser(ParserContext ctx) { this.ctx = ctx; }

    /** <expression> ::= <term> { ("+" | "-") <term> } */
    public Expr expression() {
        // TODO: build left-associative BinaryExpr nodes
        throw new UnsupportedOperationException("expression");
    }

    /** <term> ::= <factor> { ("*" | "/") <factor> } */
    private Expr term() {
        // TODO: same shape as expression()
        throw new UnsupportedOperationException("term");
    }

    /** <factor> ::= IDENTIFIER | INTEGER_CONST | REAL_CONST */
    private Expr factor() {
        // TODO: IDENTIFIER -> check ctx.identifierExists (else "undeclared variable 'x'"), return VarRef
        // TODO: INTEGER_CONST -> NumberLit(Value.of(long)); REAL_CONST -> NumberLit(Value.of(double))
        // TODO: anything else -> ctx.error("identifier or number", current) and return a placeholder
        throw new UnsupportedOperationException("factor");
    }

    /** <condition> ::= <expression> <rel_op> <expression>, rel_op = ">" | "<" | "==" */
    public Condition condition() {
        // TODO: missing relational operator -> ctx.error("relational operator ('>', '<', '==')", current); skip the right side
        throw new UnsupportedOperationException("condition");
    }
}
