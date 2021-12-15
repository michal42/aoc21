package com.company;

import java.util.ArrayList;
import java.util.List;

public class Day3 extends Puzzle {
    private int width;

    public Day3(List<String> input) {
        super(input);
        width = lines.get(0).length();
    }

    private List<Integer> filterNumbers(List<Integer> input, int filter, int mask) {
        List<Integer> output = new ArrayList<>();
        for (var n : input) {
            if ((n & mask) == filter) {
                output.add(n);
            }
        }
        return output;
    }

    private int findBit(List<Integer> input, int mask, boolean mostCommon) {
        int ones = 0;
        for (var n : input) {
            if ((n & mask) > 0) {
                ones++;
            }
        }
        if (ones * 2 >= input.size()) {
            return mostCommon ? mask : 0;
        } else {
            return mostCommon ? 0 : mask;
        }
    }

    private int findValue(List<Integer> numbers, boolean mostCommon) {
        int filter = 0;
        int mask = 0;
        for (int i = 0; i < width; i++) {
            int bit = 1 << (width - i - 1);
            mask |= bit;
            filter |= findBit(numbers, bit, mostCommon);
            numbers = filterNumbers(numbers, filter, mask);
            if (numbers.size() == 1) {
                return numbers.get(0);
            } else if (numbers.isEmpty()) {
                throw new IllegalArgumentException("empty set");
            }
        }
        throw new IllegalArgumentException("ambiguous set");
    }

    @Override
    public long runPartTwo() {
        List<Integer> numbers = new ArrayList<>();
        for (var line : lines) {
            numbers.add(Integer.parseInt(line, 2));
        }
        return findValue(numbers, true) * findValue(numbers, false);
    }

    @Override
    public long runPartOne() {
        List<Integer> zeros = new ArrayList<>();
        List<Integer> ones = new ArrayList<>();
        for (var line : lines) {
            for (int i = 0; i < line.length(); ++i) {
                if (i >= ones.size()) {
                    ones.add(0);
                    zeros.add(0);
                }
                if (line.charAt(i) == '0') {
                    zeros.set(i, zeros.get(i) + 1);
                } else {
                    ones.set(i, ones.get(i) + 1);
                }
            }
        }
        int gamma = 0;
        int epsilon = 0;
        int order = 1;
        for (int i = zeros.size() - 1; i >= 0; --i) {
            if (ones.get(i) > zeros.get(i)) {
                System.out.print("1");
                gamma += order;
            } else {
                System.out.print("0");
                epsilon += order;
            }
            order *= 2;
        }
        System.out.println();
        System.out.println("gamma: " + gamma + ", epsilon: " + epsilon);
        return gamma * epsilon;
    }

}