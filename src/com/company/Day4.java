package com.company;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day4 extends Puzzle {
    private static final int SIZE = 5;

    public class Bingo {
        public Bingo(List<String> lines) {
            assert (lines.size() == SIZE);
            for (int x = 0; x < SIZE; x++) {
                numbers[x] = Stream.of(lines.get(x).split("\\s+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
            }
        }

        public boolean draw(int n) {
            boolean[] allX = new boolean[SIZE];
            Arrays.fill(allX, true);
            for (int x = 0; x < SIZE; x++) {
                boolean allY = true;
                for (int y = 0; y < SIZE; y++) {
                    if (numbers[x][y] == n) {
                        marks[x][y] = true;
                    }
                    allY &= marks[x][y];
                    allX[y] &= marks[x][y];
                }
                if (allY) {
                    return true;
                }
            }
            for (var m : allX) {
                if (m) {
                    return true;
                }
            }
            return false;
        }

        public int getScore() {
            int res = 0;
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if (!marks[x][y]) {
                        res += numbers[x][y];
                    }
                }
            }
            return res;
        }

        @Override
        public String toString() {
            return "Bingo{" +
                    "numbers=" + Arrays.deepToString(numbers) +
                    ", marks=" + Arrays.deepToString(marks) +
                    '}';
        }

        private int[][] numbers = new int[SIZE][SIZE];
        private boolean[][] marks = new boolean[SIZE][SIZE];
    }

    private int[] draws;
    private List<Bingo> bingos;

    public Day4(List<String> lines) {
        super(lines);

        draws = Stream.of(lines.get(0).split(",")).mapToInt(Integer::parseInt).toArray();
        bingos = IntStream.range(0, lines.size() / (SIZE + 1))
                .map(i -> i * (SIZE + 1) + 2)
                .mapToObj(i -> new Bingo(lines.subList(i, i + SIZE)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public long runPartOne() {
        for (var d : draws) {
            int score = 0;
            for (var b : bingos) {
                if (b.draw(d)) {
                    score = b.getScore() * d;
                    return score;
                }
            }
        }
        return 0;

    }

    @Override
    public long runPartTwo() {
        for (var d : draws) {
            var it = bingos.listIterator();
            int score = 0;
            while (it.hasNext()) {
                var b = it.next();
                if (b.draw(d)) {
                    score = b.getScore() * d;
                    it.remove();
                }
            }
            if (bingos.isEmpty()) {
                return score;
            }
        }
        return 0;
    }
}
