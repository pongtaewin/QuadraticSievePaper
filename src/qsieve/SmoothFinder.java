package qsieve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static qsieve.Pair.*;
import static qsieve.Utility.*;

public class SmoothFinder {
    final long n;
    final long[] factorBase;
    final int sieveInterval;
    final long root;
    final int rowTol;
    final int bitTol;
    final long[] baseBits;

    public SmoothFinder(long n, long[] factorBase, int sieveInterval, long root, int rowTol, int bitTol) {
        this.n = n;
        this.factorBase = factorBase;
        this.sieveInterval = sieveInterval;
        this.root = root;
        this.rowTol = rowTol;
        this.bitTol = bitTol;
        baseBits = Arrays.stream(this.factorBase).map(p -> round(log(p) / log(2))).toArray();
    }

    Pair<List<long[]>, long[][]> findSmooth() {
        long[][] nIndices = new long[baseBits.length - 1][];
        long[][] pIndices = new long[baseBits.length - 1][];

        for (int i = 1; i < baseBits.length; i++) {
            long p = factorBase[i];
            long r = QuadraticSieve.tonelli(n, p);
            nIndices[i - 1] = new long[]{p, abs((r - root) % p - p), abs((p - r - root) % p - p)};
            pIndices[i - 1] = new long[]{p, abs(r - root) % p, abs(p - r - root) % p};
        }

        List<Long> smoothNums = List.of(), xList = List.of();
        List<long[]> factors = List.of();

        long distanceFromCenter = 0;

        while (smoothNums.size() < factorBase.length + rowTol) {

            var nSieve = makeSieve(nIndices);
            nIndices = nSieve.first;
            long[] nBits = nSieve.second;

            var pSieve = makeSieve(pIndices);
            pIndices = pSieve.first;
            long[] pBits = pSieve.second;

            var candidates = findCandidates(nBits, pBits, distanceFromCenter);
            var nResult = verifySmooth(candidates.get(0), candidates.get(1));
            var pResult = verifySmooth(candidates.get(2), candidates.get(3));

            smoothNums = toList(streamChain(toBoxedStream(nResult.first.first), smoothNums.stream(), toBoxedStream(pResult.first.first)));
            xList = toList(streamChain(toBoxedStream(nResult.first.second), xList.stream(), toBoxedStream(pResult.first.second)));
            factors = toList(streamChain(Arrays.stream(nResult.second), factors.stream(), Arrays.stream(pResult.second)));
            distanceFromCenter += sieveInterval;
        }


        return makePair(List.of(smoothNums.stream().mapToLong(l -> l).toArray(),
                xList.stream().mapToLong(l -> l).toArray()), factors.toArray(long[][]::new));
    }


    Pair<long[][], long[]> makeSieve(long[][] indices) {
        long[][] nI = new long[indices.length][];
        long[] bits = new long[sieveInterval];
        for (int k = 0; k < indices.length; k++) {
            long[] starts = indices[k];
            long p = starts[0];
            for (int i : List.of(1, 2)) {
                long start = starts[i];
                if (start >= sieveInterval) {
                    starts[i] = start - sieveInterval;
                    continue;
                }
                int j;
                for (j = (int) start; j < bits.length; j += p) bits[j] += baseBits[k + 1];
                starts[i] = j + p - sieveInterval;
            }
            nI[k] = starts;
        }
        return makePair(nI, bits);
    }

    long threshold(long x) {
        return (long) (log(abs(x * x - n)) / log(2)) - bitTol;
    }

    List<long[]> findCandidates(long[] nBits, long[] pBits, Long centerDistance) {
        ArrayList<Long> nx = new ArrayList<>(), nSmooth = new ArrayList<>(),
                px = new ArrayList<>(), pSmooth = new ArrayList<>();

        for (int i = sieveInterval - 1; i > 0; i--) {
            long x = root - i - centerDistance;
            if (x < 0) continue;
            if (abs(nBits[i]) >= threshold(root - i - centerDistance)) {
                nx.add(x);
                nSmooth.add(x * x - n);
            }
        }

        for (int i = 0; i < sieveInterval; i++) {
            long x = root + i + centerDistance;
            if (abs(pBits[i]) >= threshold(x)) {
                px.add(x);
                pSmooth.add(x * x - n);
            }
        }

        return toList(Stream.of(nx, nSmooth, px, pSmooth).map(List::stream).map(Utility::unboxedArray));
    }

    long[] getFactors(long num) {
        ArrayList<Long> factors = new ArrayList<>();
        if (num < 0) factors.add(-1L);
        num = abs(num);
        for (long p : factorBase) for (; num % p == 0; num /= p) factors.add(p);
        return abs(num) == 1 ? factors.stream().mapToLong(l -> l).toArray() : null;
    }

    Pair<Pair<long[], long[]>, long[][]> verifySmooth(long[] xCands, long[] smoothCands) {
        int[] validIndexes = iterateOn(smoothCands).filter(i -> getFactors(smoothCands[i]) != null).toArray();

        return makePair(makePair(LinearAlgebra.filterIndexes(validIndexes, smoothCands), LinearAlgebra.filterIndexes(validIndexes, xCands)),
                Arrays.stream(LinearAlgebra.filterIndexes(validIndexes, smoothCands)).mapToObj(this::getFactors).toArray(long[][]::new));
    }
}
