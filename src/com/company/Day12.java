package com.company;

import java.util.*;
import java.util.stream.Collectors;

public class Day12 extends Puzzle {
    public Day12(List<String> lines) {
        super(lines);

        // First, collect a list of lowercase(false) and uppercase(true) nodes
        var nodeGroups = lines.stream().flatMap(s -> Arrays.stream(s.split("-"))).distinct().
                collect(Collectors.partitioningBy(s -> s.matches("^[A-Z]+$")));
        int id = 0;
        for (var n : nodeGroups.get(false)) {
            nodeIds.put(n, id++);
        }
        numSmall = id;
        for (var n : nodeGroups.get(true)) {
            nodeIds.put(n, id++);
        }
        numAll = id;
        // Create an adjacency matrix
        adjacent = new boolean[numAll][numAll];
        for (var l : lines) {
            var nodes = Arrays.stream(l.split("-")).mapToInt(nodeIds::get).toArray();
            adjacent[nodes[0]][nodes[1]] = true;
            adjacent[nodes[1]][nodes[0]] = true;
        }
    }

    private boolean isSmall(int id) {
        return id < numSmall;
    }

    private long countPaths(String start, String end) {
        var startId = nodeIds.get(start);
        var endId = nodeIds.get(end);

        long res = 0;
        // -1 for the solution without free visits
        for (int i = -1; i < numSmall; i++) {
            if (i == startId || i == endId) {
                continue;
            }
            var visited = new BitSet();
            var freeVisits = new BitSet();
            if (i >= 0) {
                freeVisits.set(i);
            }
            Stack<String> path = new Stack<>();
            res += countPaths(startId, endId, visited, freeVisits);
        }
        return res;
    }

    private long countPaths(int from, int to, BitSet visited, BitSet freeVisits) {
        if (from == to) {
            // Ensure that we actually used the free visits
            if (freeVisits.isEmpty()) {
                return 1;
            } else {
                return 0;
            }
        }
        boolean clearVisit = false;
        boolean clearFreeVisit = false;
        if (isSmall(from)) {
            if (visited.get(from)) {
                if (freeVisits.get(from)) {
                    freeVisits.clear(from);
                    clearFreeVisit = true;
                } else {
                    return 0;
                }
            } else {
                visited.set(from);
                clearVisit = true;
            }
        }
        int res = 0;
        for (int i = 0; i < numAll; i++) {
            if (adjacent[from][i]) {
                res += countPaths(i, to, visited, freeVisits);
            }
        }
        if (clearVisit) {
            visited.clear(from);
        }
        if (clearFreeVisit) {
            freeVisits.set(from);
        }
        return res;
    }

    Map<String, Integer> nodeIds = new HashMap<>();
    private final int numSmall;
    private final int numAll;
    private boolean[][] adjacent;

    @Override
    public long runIt() {
        return countPaths("start", "end");
    }
}
