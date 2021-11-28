package qsieve;

import java.util.Arrays;
import java.util.List;
import java.util.stream.*;

class Utility {
    static Stream<Long> toBoxedStream(long[] array) {
        return Arrays.stream(array).boxed();
    }

    static long[] unboxedArray(Stream<Long> stream) {
        return stream.mapToLong(l -> l).toArray();
    }

    static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    static <A, R> R collectFrom(LongStream stream, Collector<Long, A, R> collector) {
        return stream.boxed().collect(collector);
    }

    @SafeVarargs
    static <T> Stream<T> streamChain(Stream<T>... streams) {
        return Arrays.stream(streams).reduce(Stream::concat).orElseThrow();
    }

    static IntStream iterateOn(int length){
        return IntStream.range(0,length);
    }

    static IntStream iterateOn(int[] array){
        return iterateOn(array.length);
    }

    static IntStream iterateOn(long[] array){
        return iterateOn(array.length);
    }

    static <E> IntStream iterateOn(E[] array){
        return iterateOn(array.length);
    }
}
