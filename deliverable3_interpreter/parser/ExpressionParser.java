package parser;

import ast.BinaryExpr;
import ast.Condition;
import ast.Expr;
import ast.NumberLit;
import ast.VarRef;
import runtime.Value;
import scanner.Token;
import scanner.TokenType;


public class ExpressionParser {
    private final ParserContext ctx;

    public ExpressionParser(ParserContext ctx) { this.ctx = ctx; }

    /** <expression> ::= <term> { ("+" | "-") <term> } */
    public Expr expression() {
        Expr left = term();
        while (isOperator("+") || isOperator("-")) {
            Token op = ctx.consume();
            Expr right = term();
            left = new BinaryExpr(op.value.charAt(0), left, right, op.line);   // left-associative
        }
        return left;
    }

    /** <term> ::= <factor> { ("*" | "/") <factor> } */
    private Expr term() {
        Expr left = factor();
        while (isOperator("*") || isOperator("/")) {
            Token op = ctx.consume();
            Expr right = factor();
            left = new BinaryExpr(op.value.charAt(0), left, right, op.line);
        }
        return left;
    }

    /** <factor> ::= IDENTIFIER | INTEGER_CONST | REAL_CONST */
    private Expr factor() {
        Token tok = ctx.current();

        if (tok != null && tok.type == TokenType.IDENTIFIER) {
            ctx.consume();
            if (!ctx.identifierExists(tok.value)) {
                ctx.parseError("undeclared variable '" + tok.value + "'", tok.line);
            }
            return new VarRef(tok.value, tok.line);
        }

        if (tok != null && tok.type == TokenType.INTEGER_CONST) {
            ctx.consume();
            try {
                return new NumberLit(Value.of(Long.parseLong(tok.value)), tok.line);
            } catch (NumberFormatException tooBig) {
                ctx.parseError("integer constant '" + tok.value + "' is too large", tok.line);
                return placeholder(tok.line);
            }
        }

        if (tok != null && tok.type == TokenType.REAL_CONST) {
            ctx.consume();
            return new NumberLit(Value.of(Double.parseDouble(tok.value)), tok.line);
        }

        // Not consumed: the caller's recovery (synchronize) decides where to resume.
        ctx.error("identifier or number", tok);
        return placeholder(tok != null ? tok.line : 0);
    }

    /** <condition> ::= <expression> <rel_op> <expression>, rel_op = ">" | "<" | "==" */
    public Condition condition() {
        Expr left = expression();
        Token tok = ctx.current();

        if (isOperator(">") || isOperator("<") || isOperator("==")) {
            ctx.consume();
            Expr right = expression();
            return new Condition(left, tok.value, right, left.line);
        }

        // One mistake, one error: skip the right side. The "?" operator is never evaluated
        // because a program with errors is never run.
        ctx.error("relational operator ('>', '<', '==')", tok);
        return new Condition(left, "?", left, left.line);
    }

    /** Stand-in node after an error so callers never see null; never evaluated (errors stop the run). */
    private static Expr placeholder(int line) { return new NumberLit(Value.of(0L), line); }

    private boolean isOperator(String op) {
        Token t = ctx.current();
        return t != null && t.type == TokenType.OPERATOR && t.value.equals(op);
    }
}