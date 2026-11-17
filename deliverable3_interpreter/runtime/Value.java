package runtime;

/** A runtime value: either an integer (long) or a real (double). No promotion between them. */
public class Value {
    private final ValueType type;
    private final long l;
    private final double d;

    private Value(ValueType type, long l, double d) { this.type = type; this.l = l; this.d = d; }

    public static Value of(long v) { return new Value(ValueType.INTEGER, v, 0); }
    public static Value of(double v) { return new Value(ValueType.REAL, 0, v); }

    public ValueType type() { return type; }
    public long asLong() { return l; }
    public double asDouble() { return d; }

    /** Integers print as-is; reals print in plain decimal form (45.95, never 4.595E1). */
    @Override
    public String toString() {
        if (type == ValueType.INTEGER) return Long.toString(l);
        if (Double.isNaN(d) || Double.isInfinite(d)) return Double.toString(d);
        String s = java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        return s.contains(".") ? s : s + ".0";   // keep ".0" so a real always looks real
    }
}