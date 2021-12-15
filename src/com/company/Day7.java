package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day7 extends Puzzle {
    public Day7(List<String> lines) {
        super(lines);

        crabs = Arrays.stream(lines.get(0).split(",")).mapToInt(Integer::parseInt).sorted().toArray();
    }

    private int[] crabs;

    @Override
    public long runPartOne() {
        int median = crabs[crabs.length / 2];
        return Arrays.stream(crabs).boxed().mapToInt(i -> Math.abs(i - median)).sum();
    }

    @Override
    public long runPartTwo() {
        int max = crabs[crabs.length - 1];
        var consumption = IntStream.range(0, max + 1).map(i -> i * (i + 1) / 2).toArray();
        var candidates = new int[max + 1];
        for (var c : crabs) {
            for (int i = 0; i <= max; i++) {
                candidates[i] += consumption[Math.abs(c - i)];
            }
        }
        return Arrays.stream(candidates).min().getAsInt();
    }
}
