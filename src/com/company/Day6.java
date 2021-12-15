package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Day6 extends Puzzle {

    public Day6(List<String> lines) {
        super(lines);

        Stream.of(lines.get(0).split(",")).mapToInt(s -> Integer.parseInt(s)).forEach(i -> initialFish[i]++);
    }

    private class FishPopulation {
        public FishPopulation() {
            this.fish = initialFish.clone();
        }

        public void step(int i) {
            long newRoe = fish[i % 7];
            fish[i % 7] += roe[i % 2];
            roe[i % 2] = newRoe;
        }

        public long size() {
            return Stream.concat(Arrays.stream(fish).boxed(), Arrays.stream(roe).boxed()).mapToLong(Long::longValue).sum();
        }

        private long[] fish;
        private long[] roe = new long[2];
    }

    private final long[] initialFish = new long[7];

    @Override
    public long runPartOne() {
        var population = new FishPopulation();
        for (int i = 0; i < 80; i++) {
            population.step(i);
        }
        return population.size();
    }

    @Override
    public long runPartTwo() {
        var population = new FishPopulation();
        for (int i = 0; i < 256; i++) {
            population.step(i);
        }
        return population.size();
    }
}
