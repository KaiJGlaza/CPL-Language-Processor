package parser;

import ast.Declaration;
import ast.Program;
import ast.Statement;
import java.util.List;
import scanner.Token;

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
        // TODO: return start()
        throw new UnsupportedOperationException("Parser.begin");
    }

    /** <program> ::= <import> "implementations" <function> */
    private Program start() {
        // TODO: importStmt(); ctx.match("implementations"); build the Program from functionDef()
        // TODO: any token after "endfun main" -> ctx.error("end of file after 'endfun main'", token)
        throw new UnsupportedOperationException("start");
    }

    /** <import> ::= "import" STRING */
    private String importStmt() {
        // TODO: match "import", match a STRING token, return its value
        throw new UnsupportedOperationException("importStmt");
    }

    /** "function" "main" "return" "type" <type> "is" "variables" <declarations> "begin" <statements> "endfun" "main" */
    private Program functionDef(String importName) {
        // TODO: match the header keywords; read the return type; declParser.declarations()
        // TODO: match "begin"; stmtParser.actions("'begin' block"); match "endfun" then "main"
        throw new UnsupportedOperationException("functionDef");
    }
}
