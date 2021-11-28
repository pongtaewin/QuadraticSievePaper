package qsieve;

class Pair<E1, E2> {
    E1 first;
    E2 second;

    Pair(E1 x, E2 y) {
        first = x;
        second = y;
    }

    static <E1,E2> Pair<E1, E2> makePair(E1 x, E2 y) {
        return new Pair<>(x, y);
    }
}
