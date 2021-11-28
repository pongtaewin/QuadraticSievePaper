package qsieve;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.lang.Math.*;
import static qsieve.Pair.makePair;
import static qsieve.Utility.*;

public class QuadraticSieve {

    static long generateSemiPrime(long origin, long bound, long seed) {
        return new Random(seed).longs(origin, bound).filter(l -> isProbablePrime(l, seed, 5))
                .limit(2).reduce((x, y) -> x * y).orElseThrow();
    }

    static long gcd(long a, long b) {
        return b == 0 ? a : a < b ? gcd(b, a) : gcd(b, a % b);
    }

    static long floorSqrt(long n) {
        long p = n;
        long q = (p + 1) / 2;
        while (p > q) {
            p = q;
            q = (p + n / p) / 2;
        }
        return p;
    }

    static long[] basicSievePrimes(int max) {
        Boolean[] isPrime = IntStream.rangeClosed(0, max).mapToObj(i -> i >= 2).toArray(Boolean[]::new);
        for (int i = 0; i <= max / 2; i++) if (isPrime[i]) for (int j = 2 * i; j <= max; j += i) isPrime[j] = false;
        return IntStream.rangeClosed(0, max).filter(i -> isPrime[i]).mapToLong(i -> i).toArray();
    }

    static long legendre(long a, long p) {
        return ModOperators.mod(p).pow(a, (p - 1) / 2);
    }

    static long tonelli(long n, long p) {
        if (legendre(n, p) != 1) throw new AssertionError();
        ModOperators modP = new ModOperators(p);
        long q = p - 1;
        long s = Long.numberOfTrailingZeros(q);
        q /= (1L << s);
        if (s == 1) return modP.pow(n, (p + 1) / 4);
        long z;
        for (z = 2; z < p; z++) if (legendre(z, p) == p - 1) break;

        long c = modP.pow(z, q);
        long r = modP.pow(n, (q + 1) / 2);
        long t = modP.pow(n, q);
        long m = s;
        long t2;
        while ((t - 1) % p != 0) {
            t2 = modP.multiply(t, t);
            int i;
            for (i = 1; i < m; i++) {
                if ((t2 - 1) % p == 0) break;
                t2 = modP.multiply(t2, t2);
            }
            long b = modP.pow(c, 1L << (m - i - 1));
            r = modP.multiply(r, b);
            c = modP.multiply(b, b);
            t = modP.multiply(t, c);
            m = i;
        }
        return r;
    }

    static long[] findFactorBase(long n, int b) {
        return Arrays.stream(basicSievePrimes(b)).filter(p -> legendre(n, p) == 1).toArray();
    }

    static long[] toExponentVector(long[] factors, long[] factorBase) {
        long[] expVector = new long[factorBase.length];
        Arrays.fill(expVector, 0);
        Map<Long, Integer> map = iterateOn(factorBase).boxed().collect(Collectors.toMap(i -> factorBase[i], i -> i));
        Arrays.stream(factors).filter(map::containsKey).forEach(factor -> expVector[map.get(factor)]++);
        return Arrays.stream(expVector).map(i -> i % 2).toArray();
    }

    static Pair<Long, long[][]> buildMatrix(long[] factorBase, long[] smoothNums, long[][] factors) {
        long[][] m = new long[smoothNums.length][];
        factorBase = LongStream.concat(LongStream.of(-1), Arrays.stream(factorBase)).toArray();

        for (int i = 0; i < smoothNums.length; i++) {
            long[] exp = toExponentVector(factors[i], factorBase);
            if (Arrays.stream(exp).noneMatch(l -> l == 1)) return makePair(smoothNums[i], null);
            m[i] = exp;
        }
        return makePair(null, LinearAlgebra.transposeMatrix(m));
    }

    static long solve(long n, long[] solution, long[] smoothNums, long[] xList) {
        long[] solutionNums = Arrays.stream(solution).map(l -> smoothNums[(int) l]).toArray();
        long[] xNums = Arrays.stream(solution).map(l -> xList[(int) l]).toArray();

        ModOperators modN = ModOperators.mod(n);
        long b = 1;
        for (long x : xNums) b = modN.multiply(x, b);

        long[] lst = Arrays.stream(solutionNums).map(Math::abs).toArray();

        long a = 1;
        Set<Long> set = Arrays.stream(basicSievePrimes(500)).boxed().collect(Collectors.toSet());
        while (lst.length != 0) {
            for (int i = 0; i < lst.length; i++) {
                if (isProbablePrime(abs(lst[i]), i, 5)) set.add(lst[i]);
                for (int j = i + 1; j < lst.length; j++) {
                    long gcd = gcd(abs(lst[i]), abs(lst[j]));
                    if (gcd != 1 && !set.contains(gcd) && isProbablePrime(gcd, (long) i * j, 5)) set.add(gcd);
                }
            }
            if (set.isEmpty()) {
                for (long l : lst) if (isSquare(l)) set.add(floorSqrt(l));
                if (set.isEmpty()) set.add(lst[0]);
            }
            for (long p : set) {
                int count = 0;
                for (int i = 0; i < lst.length; i++)
                    while (lst[i] % p == 0) {
                        lst[i] /= p;
                        count++;
                    }
                if (count % 2 != 0) throw new AssertionError();
                a = modN.multiply(modN.pow(abs(p), count / 2), a);
            }
            lst = Arrays.stream(lst).filter(l -> l != 1 && l != -1).toArray();
            set.clear();
        }
        return gcd(abs(b - a), n);
    }

    static boolean isSquare(long n) {
        return n % floorSqrt(n) == 0;
    }

    static long[] quadraticSieve(long n, Integer b, Integer sieveInterval) throws ValueNotFoundException {
        Random rand = new Random();
        if (isProbablePrime(n, rand.nextLong(), 5)) throw new AssertionError();

        Long primePow = LongStream.range(2, (long) (log(n) / log(2)))
                .mapToObj(power -> {
                    long r = (long) (1000 * pow(n, 1 / (double) power)) / 1000;
                    return pow(r, power) == n ? r : null;
                }).filter(Objects::nonNull).findFirst().orElse(null);
        if (primePow != null) return LongStream.generate(() -> primePow).limit(round(log(n) / log(primePow))).toArray();

        long root = floorSqrt(n);
        int rowTol = 0, bitTol = 20;

        long[] factorBase = findFactorBase(n, b);
        int f = factorBase.length;

        var smoothResult = new SmoothFinder(n, factorBase, sieveInterval, root, rowTol, bitTol).findSmooth();
        long[] smoothNums = smoothResult.first.get(0);
        long[] xList = smoothResult.first.get(1);
        long[][] factors = smoothResult.second;
        if (smoothNums.length < f) throw new IllegalArgumentException();

        if (f < smoothNums.length - 100) {
            smoothNums = Arrays.stream(smoothNums).limit(f + rowTol).toArray();
            xList = Arrays.stream(xList).limit(f + rowTol).toArray();
            factors = Arrays.stream(factors).limit(f + rowTol).toArray(long[][]::new);
        }

        var matrixResult = buildMatrix(factorBase, smoothNums, factors);
        if (matrixResult.first != null) {
            long sq = matrixResult.first;
            final long[] sN = smoothNums;
            int x = iterateOn(smoothNums).filter(i -> sN[i] == sq).findFirst().orElseThrow();
            long factor = gcd(xList[x] + floorSqrt(sq), n);
            return new long[]{factor, n / factor};
        }
        long[][] matrix = matrixResult.second;

        var gaussianResult = LinearAlgebra.gaussianElimination(matrix);
        List<Pair<long[], Long>> solutions = gaussianResult.first;
        int[] marks = gaussianResult.second.first;
        long[][] resultMatrix = gaussianResult.second.second;

        long[] solution = LinearAlgebra.solveRow(solutions, resultMatrix, marks, 0);
        factorBase = Arrays.stream(factorBase).filter(l -> l != -1).toArray();

        long factor = solve(n, solution, smoothNums, xList);

        for (int k = 1; k < solutions.size(); k++) {
            if (factor == 1 || factor == n) {
                solution = LinearAlgebra.solveRow(solutions, resultMatrix, marks, k);
                factor = solve(n, solution, smoothNums, xList);
            } else return new long[]{factor, n / factor};
        }
        if (factor == 1 || factor == n) throw new ValueNotFoundException();
        return new long[]{factor, n / factor};
    }

    static long[] quadraticSieve(long n) {
        ArrayList<Long> result = new ArrayList<>();
        PriorityQueue<Long> heap = new PriorityQueue<>(List.of(n));
        while (!heap.isEmpty()) {
            long toFactor = heap.poll();
            if (isProbablePrime(toFactor, toFactor, 10)) {
                result.add(toFactor);
                continue;
            }
            long[] factorResult;
            for (int b = 30; ; b++) {
                try {
                    factorResult = quadraticSieve(toFactor, b * b, b * b);
                } catch (ValueNotFoundException e) {
                    System.out.print("x");
                    continue;
                }
                break;
            }

            heap.addAll(toList(toBoxedStream(factorResult)));
        }
        return result.stream().mapToLong(l -> l).sorted().toArray();
    }

    static boolean isProbablePrime(long n, long seed, int cycles) {
        if (n < 1) throw new AssertionError();
        if (n % 2 == 0) return n == 2;
        int s = Long.numberOfTrailingZeros(n - 1);
        long d = (n - 1) / (1L << s);
        if (d * (1L << s) != n - 1) throw new AssertionError();

        ModOperators modN = ModOperators.mod(n);
        return new Random(seed).longs(cycles, 2, n).noneMatch(
                a -> !(modN.pow(a, d) == 1) && iterateOn(s).noneMatch(
                        i -> modN.pow(a, d * (1L << i)) == n - 1));
    }

    static String printFactors(long n) {
        return n + ": " + Arrays.toString(quadraticSieve(n));
    }


    public static void main(String[] args) {
        System.out.println(printFactors(7387));
    }

}
