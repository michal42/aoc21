package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Day11 extends  Puzzle {
    public Day11(List<String> lines) {
        super(lines);

        assert (lines.size() == HEIGHT);
        int y = 0;
        for (var l : lines) {
            assert l.length() == WIDTH;
            grid[y++] = Arrays.stream(l.split("")).mapToInt(Integer::parseInt).toArray();
        }
    }

    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    int[][] grid = new int[HEIGHT][];

    private int charge(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return 0;
        }
        int res = 0;
        if (++grid[y][x] == 10) {
            res++;
            res += charge(x - 1, y - 1);
            res += charge(x, y - 1);
            res += charge(x + 1, y - 1);

            res += charge(x - 1, y);
            res += charge(x + 1, y);

            res += charge(x - 1, y + 1);
            res += charge(x, y + 1);
            res += charge(x + 1, y + 1);
        }
        return res;
    }

    private void adjust(int x, int y) {
        if (grid[y][x] > 9) {
            grid[y][x] = 0;
        }
    }

    private int step() {
        int res = 0;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                adjust(x, y);
            }
        }
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                res += charge(x, y);
            }
        }
        return res;
    }

    @Override
    public long runIt() {
        //return IntStream.range(0, 100).map(i -> step()).sum();
        long res = 1;
        while (step() != 100) {
            res++;
        }
        return res;
    }
}
