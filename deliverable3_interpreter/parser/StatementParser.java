package parser;

import ast.Statement;
import java.util.List;

/** <statements> and every statement rule. Builds Statement nodes. */
public class StatementParser {
    private final ParserContext ctx;
    private final ExpressionParser exprs;

    public StatementParser(ParserContext ctx, ExpressionParser exprs) {
        this.ctx = ctx;
        this.exprs = exprs;
    }

    /**
     * Read statements until endfun, else, or endif (not consumed). An empty block is an error:
     * "expected at least one statement in <blockName>".
     * After a bad statement, synchronize at the next statement keyword or terminator.
     */
    public List<Statement> actions(String blockName) {
        // TODO: loop; dispatch on the leading keyword; token that cannot start a statement -> ctx.error("a statement", tok)
        throw new UnsupportedOperationException("actions");
    }

    /** "set" IDENTIFIER "=" <expression>  (name must be declared) */
    private Statement setStmt() { throw new UnsupportedOperationException("setStmt"); }

    /** "display" <item> { "," <item> }  (item = STRING | expression) */
    private Statement displayStmt() { throw new UnsupportedOperationException("displayStmt"); }

    /** "input" STRING "," IDENTIFIER  (name must be declared) */
    private Statement inputStmt() { throw new UnsupportedOperationException("inputStmt"); }

    /** "return" <expression> */
    private Statement returnStmt() { throw new UnsupportedOperationException("returnStmt"); }

    /** "exit" */
    private Statement exitStmt() { throw new UnsupportedOperationException("exitStmt"); }

    /**
     * "if" <condition> "then" <statements> [ "else" <statements> ] "endif".
     * Nests to any depth because actions() calls this again on another "if".
     */
    private Statement ifStmt() { throw new UnsupportedOperationException("ifStmt"); }
}
