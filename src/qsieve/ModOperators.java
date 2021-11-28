package qsieve;

public class ModOperators {

    final long z;

    public ModOperators(long z) {
        this.z = z;
    }

    static ModOperators mod(long z){
        return new ModOperators(z);
    }

    long multiply(long x, long y) {
        return x > z || y > z ? multiply(x % z, y % z) : _multiply(x, y);
    }

    private long _multiply(long x, long y) {
        long res = 0; // Initialize result
        while (y > 0) {
            if (y % 2 == 1) res = (res + x) % z;
            x = (x * 2) % z;
            y /= 2;
        }
        return res % z;
    }

    long pow(long x, long y) {
        if (x < 0 || y < 0 || z < 2) throw new AssertionError();
        if (x > z) return pow(x % z, y);
        if (y < 2) return y == 0 ? 1 : x;
        long lower = pow(x, y / 2);
        return multiply(y % 2 == 1 ? x : 1, multiply(lower, lower));
    }
}
