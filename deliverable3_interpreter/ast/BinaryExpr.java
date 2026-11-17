package ast;

import runtime.Memory;
import runtime.RuntimeError;
import runtime.Value;
import runtime.ValueType;

/** left op right, for + - * /. */
public class BinaryExpr extends Expr {
    public final char op;
    public final Expr left, right;

    public BinaryExpr(char op, Expr left, Expr right, int line) {
        super(line);
        this.op = op; this.left = left; this.right = right;
    }

    @Override
    public Value eval(Memory m) {
        Value a = left.eval(m);
        Value b = right.eval(m);

        // No promotion: both sides must be the same type.
        if (a.type() != b.type()) {
            throw new RuntimeError("cannot mix integer and real in '" + a + " " + op + " " + b + "'", line);
        }

        if (a.type() == ValueType.INTEGER) {
            long x = a.asLong(), y = b.asLong();
            switch (op) {
                case '+': return Value.of(x + y);
                case '-': return Value.of(x - y);
                case '*': return Value.of(x * y);
                case '/':
                    if (y == 0) throw new RuntimeError("division by zero", line);
                    return Value.of(x / y);          // integer division truncates toward zero
            }
        } else {
            double x = a.asDouble(), y = b.asDouble();
            switch (op) {
                case '+': return Value.of(x + y);
                case '-': return Value.of(x - y);
                case '*': return Value.of(x * y);
                case '/':
                    if (y == 0.0) throw new RuntimeError("division by zero", line);
                    return Value.of(x / y);
            }
        }
        throw new IllegalStateException("unknown operator '" + op + "'");
    }
}