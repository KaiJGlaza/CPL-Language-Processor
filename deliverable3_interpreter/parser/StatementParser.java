package parser;

import ast.Condition;
import ast.DisplayStmt;
import ast.ExitStmt;
import ast.Expr;
import ast.IfStmt;
import ast.InputStmt;
import ast.ReturnStmt;
import ast.SetStmt;
import ast.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import scanner.Token;
import scanner.TokenType;

/** <statements> and every statement rule. Builds Statement nodes. */
public class StatementParser {
    /** Recovery after a bad if-condition: stop at its "then" or at any statement keyword or terminator. */
    private static final Set<String> AFTER_BAD_CONDITION = new HashSet<>(ParserContext.STATEMENT_SYNC);
    static { AFTER_BAD_CONDITION.add("then"); }

    private final ParserContext ctx;
    private final ExpressionParser exprs;

    public StatementParser(ParserContext ctx, ExpressionParser exprs) {
        this.ctx = ctx;
        this.exprs = exprs;
    }

    /**
     * Read statements until endfun, else, or endif (not consumed). 
     * After a bad statement, synchronize at the next statement keyword or terminator.
     */
    public List<Statement> actions(String blockName) {
        List<Statement> result = new ArrayList<>();
        boolean attempted = false;

        while (true) {
            Token tok = ctx.current();
            if (tok == null) break;                                  // end of the token stream
            if (isKeyword(tok, "endfun") || isKeyword(tok, "else") || isKeyword(tok, "endif")) {
                break;                                               // block terminator: stop, do not consume
            }
            attempted = true;

            if (startsStatement(tok)) {
                int errorsBefore = ctx.errorCount();
                Statement s = statement(tok);
                if (s != null) result.add(s);                        // null = the statement had an error
                if (ctx.errorCount() > errorsBefore) {
                    ctx.synchronize(ParserContext.STATEMENT_SYNC);   // skip to a safe place
                }
            } else {
                // A token that cannot start a statement: report it, skip it, resynchronize.
                ctx.error("a statement", tok);
                ctx.consume();
                ctx.synchronize(ParserContext.STATEMENT_SYNC);
            }
        }

        if (!attempted) {
            ctx.error("at least one statement in " + blockName, ctx.current());
        }
        return result;
    }

    /** Dispatch on the leading keyword (already known to start a statement). */
    private Statement statement(Token tok) {
        switch (tok.value) {
            case "set":     return setStmt();
            case "display": return displayStmt();
            case "input":   return inputStmt();
            case "return":  return returnStmt();
            case "exit":    return exitStmt();
            default:        return ifStmt();   // "if"
        }
    }

    /** "set" IDENTIFIER "=" <expression>  (name must be declared) */
    private Statement setStmt() {
        int line = ctx.current().line;
        int errorsBefore = ctx.errorCount();
        ctx.match("set");

        Token name = ctx.matchType(TokenType.IDENTIFIER);
        checkDeclared(name);

        ctx.match("=");
        Expr value = exprs.expression();

        if (name == null || ctx.errorCount() > errorsBefore) return null;
        return new SetStmt(name.value, value, line);
    }

    /** "display" <item> { "," <item> }  (item = STRING | expression) */
    private Statement displayStmt() {
        int line = ctx.current().line;
        int errorsBefore = ctx.errorCount();
        ctx.match("display");

        List<DisplayStmt.Item> items = new ArrayList<>();
        items.add(displayItem());
        while (isOperator(ctx.current(), ",")) {
            ctx.consume();                       // eat ','
            items.add(displayItem());
        }

        if (ctx.errorCount() > errorsBefore) return null;
        return new DisplayStmt(items, line);
    }

    /** One display item: a string literal or an expression. */
    private DisplayStmt.Item displayItem() {
        Token tok = ctx.current();
        if (tok != null && tok.type == TokenType.STRING) {
            ctx.consume();
            return new DisplayStmt.Item(tok.value, null);
        }
        return new DisplayStmt.Item(null, exprs.expression());
    }

    /** "input" STRING "," IDENTIFIER  (name must be declared) */
    private Statement inputStmt() {
        int line = ctx.current().line;
        int errorsBefore = ctx.errorCount();
        ctx.match("input");

        Token prompt = ctx.matchType(TokenType.STRING);
        ctx.match(",");
        Token name = ctx.matchType(TokenType.IDENTIFIER);
        checkDeclared(name);

        if (prompt == null || name == null || ctx.errorCount() > errorsBefore) return null;
        return new InputStmt(prompt.value, name.value, line);
    }

    /** "return" <expression> */
    private Statement returnStmt() {
        int line = ctx.current().line;
        int errorsBefore = ctx.errorCount();
        ctx.match("return");
        Expr value = exprs.expression();

        if (ctx.errorCount() > errorsBefore) return null;
        return new ReturnStmt(value, line);
    }

    /** "exit" */
    private Statement exitStmt() {
        int line = ctx.current().line;
        ctx.match("exit");
        return new ExitStmt(line);
    }

    /**
     * "if" <condition> "then" <statements> [ "else" <statements> ] "endif".
     * Nests to any depth because actions() calls this again on another "if".
     */
    private Statement ifStmt() {
        int line = ctx.current().line;
        int errorsBefore = ctx.errorCount();
        ctx.match("if");
        Condition cond = exprs.condition();

        if (ctx.errorCount() > errorsBefore) {
            // The condition was bad: skip ahead to its "then" so one mistake gives one error.
            ctx.synchronize(AFTER_BAD_CONDITION);
            if (isKeyword(ctx.current(), "then")) ctx.consume();
        } else {
            ctx.match("then");
        }

        List<Statement> thenBlock = actions("'then' branch");   // recurses for nested ifs
        List<Statement> elseBlock = new ArrayList<>();           // empty list when there is no else

        if (isKeyword(ctx.current(), "else")) {
            ctx.consume();
            elseBlock = actions("'else' branch");
        }

        ctx.match("endif");

        if (ctx.errorCount() > errorsBefore) return null;
        return new IfStmt(cond, thenBlock, elseBlock, line);
    }

    // ---- helpers ----

    /** The variable must have been declared with define. */
    private void checkDeclared(Token name) {
        if (name != null && !ctx.identifierExists(name.value)) {
            ctx.parseError("undeclared variable '" + name.value + "'", name.line);
        }
    }

    private static boolean startsStatement(Token t) {
        if (t.type != TokenType.KEYWORD) return false;
        switch (t.value) {
            case "set": case "display": case "input": case "return": case "exit": case "if": return true;
            default: return false;
        }
    }

    private static boolean isKeyword(Token t, String kw) {
        return t != null && t.type == TokenType.KEYWORD && t.value.equals(kw);
    }

    private static boolean isOperator(Token t, String op) {
        return t != null && t.type == TokenType.OPERATOR && t.value.equals(op);
    }
}