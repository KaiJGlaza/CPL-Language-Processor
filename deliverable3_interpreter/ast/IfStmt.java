package ast;

import java.util.List;
import runtime.Interpreter;

/** if <condition> then <statements> [ else <statements> ] endif. elseBlock is an empty list when there is no else. */
public class IfStmt extends Statement {
    public final Condition cond;
    public final List<Statement> thenBlock, elseBlock;

    public IfStmt(Condition cond, List<Statement> thenBlock, List<Statement> elseBlock, int line) {
        super(line);
        this.cond = cond; this.thenBlock = thenBlock; this.elseBlock = elseBlock;
    }

    @Override
    public void execute(Interpreter in) {
        if (cond.eval(in.memory())) {
            in.executeBlock(thenBlock);
        } else {
            in.executeBlock(elseBlock);   // an empty list when there is no else
        }
    }
}