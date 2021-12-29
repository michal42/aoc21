package com.company;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day15 extends Puzzle {
    public Day15(List<String> lines) {
        super(lines);
        riskMap = lines.stream().filter(Predicate.not(String::isEmpty))
                .map(l -> Arrays.stream(l.split("")).mapToInt(Integer::parseInt).toArray()).toArray(int[][]::new);
        width = riskMap[0].length;
        height = riskMap.length;
    }

    private final int[][] riskMap;
    private final int width;
    private final int height;

    private static class Bfs {
        public Bfs(int[][] riskMap) {
            this.riskMap = riskMap;
            width = riskMap[0].length;
            height = riskMap.length;
        }

        private int[][] runBfs(Position start) {
            int[][] costs = new int[height][width];
            Queue<Position> queue = new ArrayDeque<>();
            Arrays.stream(costs).forEach(a -> Arrays.fill(a, -1));
            costs[start.getY()][start.getX()] = 0;
            queue.add(start);
            while (!queue.isEmpty()) {
                var pos = queue.remove();
                var from = costs[pos.getY()][pos.getX()];
                for (var adj : getAdjacent(pos)) {
                    var target = costs[adj.getY()][adj.getX()];
                    if (target < 0 || target > from + riskMap[adj.getY()][adj.getX()]) {
                        costs[adj.getY()][adj.getX()] = from + riskMap[adj.getY()][adj.getX()];
                        queue.add(adj);
                    }
                }
            }
            return costs;
        }

        private List<Position> getAdjacent(Position pos) {
            List<Position> res = new ArrayList<>();
            var x = pos.getX();
            var y = pos.getY();
            if (x > 0) {
                res.add(new Position(x - 1, y));
            }
            if (x < width - 1) {
                res.add(new Position(x + 1, y));
            }
            if (y > 0) {
                res.add(new Position(x, y - 1));
            }
            if (y < height - 1) {
                res.add(new Position(x, y + 1));
            }
            return res;
        }

        private final int[][] riskMap;
        private final int width;
        private final int height;
    }

    @Override
    public long runPartOne() {
        var bfs = new Bfs(riskMap);
        var costs = bfs.runBfs(new Position(0, 0));
        return costs[height - 1][width - 1];
    }

    @Override
    public long runPartTwo() {
        var large = new int[height * 5][width * 5];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int my = 0; my < 5; my++) {
                    for (int mx = 0; mx < 5; mx++) {
                        large[my * height + y][mx * width + x] = (riskMap[y][x] - 1 + mx + my) % 9 + 1;
                    }
                }
            }
        }
        var bfs = new Bfs(large);
        var costs = bfs.runBfs(new Position(0, 0));
        return costs[height * 5 - 1][width * 5 - 1];
    }
}
