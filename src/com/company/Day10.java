package com.company;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

public class Day10 extends Puzzle {
    public Day10(List<String> lines) {
        super(lines);
    }

    private static final Map<Character, Integer> costUnexpected = Map.of(
            ')', 3,
            ']', 57,
            '}', 1197,
            '>', 25137
    );
    private static final Map<Character, Integer> costMissing = Map.of(
            ')', 1,
            ']', 2,
            '}', 3,
            '>', 4
    );

    private long parseLine(String line, boolean evaluateUnexpected) {
        Stack<Character> expect = new Stack<>();

        for (var c : line.toCharArray()) {
            switch (c) {
                case '(':
                    expect.push(')');
                    break;
                case '[':
                    expect.push(']');
                    break;
                case '{':
                    expect.push('}');
                    break;
                case '<':
                    expect.push('>');
                    break;
                case ')':
                case ']':
                case '}':
                case '>':
                    if (c != expect.pop()) {
                        if (evaluateUnexpected) {
                            return costUnexpected.get(c);
                        } else {
                            return 0;
                        }
                    }
            }
        }
        if (evaluateUnexpected) {
            return 0;
        }
        long score = 0;
        while (!expect.empty()) {
            score *= 5;
            score += costMissing.get(expect.pop());
        }
        return score;
    }

    @Override
    public long runPartOne() {
        return lines.stream().mapToLong(l -> parseLine(l, true)).sum();
    }

    @Override
    public long runPartTwo() {
        var scores = lines.stream().mapToLong(l -> parseLine(l, false)).filter(i -> i != 0).sorted().toArray();
        return scores[scores.length / 2];
    }
}
