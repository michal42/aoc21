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

    private long countPaths(String start, String end, boolean doExtraVisits) {
        var startId = nodeIds.get(start);
        var endId = nodeIds.get(end);

        long res = countPaths(startId, endId, new BitSet(), new BitSet());
        if (!doExtraVisits) {
            return res;
        }
        for (int i = 0; i < numSmall; i++) {
            if (i == startId || i == endId) {
                continue;
            }
            var extra = new BitSet();
            if (i >= 0) {
                extra.set(i);
            }
            res += countPaths(startId, endId, new BitSet(), extra);
        }
        return res;
    }

    private long countPaths(int from, int to, BitSet visited, BitSet extraVisits) {
        if (from == to) {
            // Ensure that we actually used the extra visits
            if (extraVisits.isEmpty()) {
                return 1;
            } else {
                return 0;
            }
        }
        boolean popVisit = false;
        boolean popExtraVisit = false;
        if (isSmall(from)) {
            if (visited.get(from)) {
                if (extraVisits.get(from)) {
                    extraVisits.clear(from);
                    popExtraVisit = true;
                } else {
                    return 0;
                }
            } else {
                visited.set(from);
                popVisit = true;
            }
        }
        int res = 0;
        for (int i = 0; i < numAll; i++) {
            if (adjacent[from][i]) {
                res += countPaths(i, to, visited, extraVisits);
            }
        }
        if (popVisit) {
            visited.clear(from);
        }
        if (popExtraVisit) {
            extraVisits.set(from);
        }
        return res;
    }

    Map<String, Integer> nodeIds = new HashMap<>();
    private final int numSmall;
    private final int numAll;
    private boolean[][] adjacent;

    @Override
    public long runPartOne() {
        return countPaths("start", "end", false);
    }

    @Override
    public long runPartTwo() {
        return countPaths("start", "end", true);
    }
}
