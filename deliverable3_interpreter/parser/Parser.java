package parser;

import ast.Declaration;
import ast.Program;
import ast.Statement;
import java.util.List;
import runtime.ValueType;
import scanner.Token;
import scanner.TokenType;

/**
 * Top of the grammar. Keeps D2's three public functions: getNextToken(), identifierExists(), begin().
 * Everything below start() is private, one method per rule; declarations, statements, and
 * expressions live in their own classes that share one ParserContext.
 */
public class Parser {
    private final ParserContext ctx;
    private final DeclarationParser declParser;
    private final StatementParser stmtParser;

    public Parser(List<Token> tokens) {
        this.ctx = new ParserContext(tokens);
        ExpressionParser exprParser = new ExpressionParser(ctx);
        this.declParser = new DeclarationParser(ctx);
        this.stmtParser = new StatementParser(ctx, exprParser);
    }

    public Token getNextToken() { return ctx.getNextToken(); }
    public boolean identifierExists(String identifier) { return ctx.identifierExists(identifier); }
    public int errorCount() { return ctx.errorCount(); }

    /** Parse the whole program. The Program is only usable when errorCount() == 0. */
    public Program begin() {
        return start();
    }

    /** <program> ::= <import> "implementations" <function> */
    private Program start() {
        String importName = importStmt();
        ctx.match("implementations");
        Program program = functionDef(importName);

        // Nothing may follow "endfun main".
        Token extra = ctx.current();
        if (extra != null) {
            ctx.error("end of file after 'endfun main'", extra);
        }
        return program;
    }

    /** <import> ::= "import" STRING */
    private String importStmt() {
        ctx.match("import");
        Token name = ctx.matchType(TokenType.STRING);
        return name != null ? name.value : null;   // null only when there was a syntax error
    }

    /** "function" "main" "return" "type" <type> "is" "variables" <declarations> "begin" <statements> "endfun" "main" */
    private Program functionDef(String importName) {
        ctx.match("function");
        ctx.match("main");
        ctx.match("return");
        ctx.match("type");
        ValueType returnType = declParser.type();
        ctx.match("is");
        ctx.match("variables");
        List<Declaration> declarations = declParser.declarations();
        ctx.match("begin");
        List<Statement> statements = stmtParser.actions("'begin' block");
        if (ctx.match("endfun") != null) {
            ctx.match("main");
        }
        return new Program(importName, returnType, declarations, statements);
    }
}