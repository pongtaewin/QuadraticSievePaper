package qsieve;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static qsieve.Pair.makePair;
import static qsieve.Utility.iterateOn;
import static qsieve.Utility.toList;

class LinearAlgebra {
    static long[][] transposeMatrix(final long[][] matrix) {
        return iterateOn(matrix[0])
                .mapToObj(j -> Arrays.stream(matrix).mapToLong(row -> row[j]).toArray()).toArray(long[][]::new);
    }

    static Pair<List<Pair<long[], Long>>, Pair<int[], long[][]>> gaussianElimination
            (final long[][] m) throws ValueNotFoundException {
        int[] marks = new int[m[0].length];
        for (int i : iterateOn(m).toArray()) {
            long[] row = m[i];
            int j = iterateOn(row).filter(num -> row[num] == 1).findFirst().orElse(-1);
            if (j == -1) continue;
            marks[j] = 1;
            for (int k : iterateOn(m).filter(k_ -> k_ != i && m[k_][j] == 1).toArray())
                for (int l = 0; l < m[k].length; l++) m[k][l] = (m[k][l] + row[l]) % 2;
        }
        final long[][] mt = transposeMatrix(m);

        List<Pair<long[], Long>> solutions = toList(iterateOn(marks).filter(i -> marks[i] == 0)
                .mapToObj(i -> makePair(mt[i], (long) i)));
        if (solutions.isEmpty()) throw new ValueNotFoundException();
        return makePair(solutions, makePair(marks, mt));
    }

    static long[] solveRow(List<Pair<long[], Long>> solutions, long[][] matrix, int[] marks, int k) {
        long[] freeRow = solutions.get(k).first;
        List<Long> indices = iterateOn(freeRow)
                .filter(i -> freeRow[i] == 1).mapToObj(i -> (long) i).collect(Collectors.toList());

        return LongStream.concat(iterateOn(matrix).filter(r ->
                        indices.stream().mapToInt(Long::intValue).anyMatch(i -> matrix[r][i] == 1 && marks[r] == 1))
                .mapToLong(r -> (long) r), LongStream.of(solutions.get(k).second)).toArray();
    }

    static long[] filterIndexes(int[] indexes, long[] values) {
        return Arrays.stream(indexes).mapToLong(i -> values[i]).toArray();
    }
}
