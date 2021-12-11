package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Day6 extends Puzzle {

    public Day6(List<String> lines) {
        super(lines);

        Stream.of(lines.get(0).split(",")).mapToInt(s -> Integer.parseInt(s)).forEach(i -> fish[i]++);
    }

    private long[] fish = new long[7];
    private long[] roe = new long[2];


    @Override
    public long runIt() {
        for (int i = 0; i < 256; i++) {
            long newRoe = fish[i % 7];
            fish[i % 7] += roe[i % 2];
            roe[i % 2] = newRoe;
        }

        return Stream.concat(Arrays.stream(fish).boxed(), Arrays.stream(roe).boxed()).mapToLong(Long::longValue).sum();
    }
}
