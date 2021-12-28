package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// FIXME: The algorithm works fine the example input, it also works with my personalized input for 10 iterations,
// but it disagrees with AoC by an offset of 1 about the result after 40 iterations (AoC's result is 1 less than mine)

public class Day14 extends Puzzle {
    public Day14(List<String> lines) {
        super(lines);

        var template = lines.get(0);
        initialPairs = IntStream.range(0, template.length() - 1).map(i -> encodePair(template.substring(i, i + 2))).toArray();
        rules = lines.stream().skip(2).takeWhile(Predicate.not(String::isEmpty)).collect(Collectors.toMap(Day14::encodePair, Day14::encodeRuleResult));
    }

    private final int[] initialPairs;
    private final Map<Integer, List<Integer>> rules;
    private static int CODE_BITS = 5;

    private static int encodePair(String pair) {
        return encodePair(pair.charAt(0), pair.charAt(1));
    }

    private static String decodePair(int pair) {
        return "" + (char)((pair >> CODE_BITS) + 'A') + (char)((pair & ((1 << CODE_BITS) - 1)) + 'A');
    }

    private static int encodePair(char c1, char c2) {
        return (c1 - 'A') << CODE_BITS | (c2 - 'A');
    }

    private static List<Integer> encodeRuleResult(String rule) {
        assert rule.length() == 7;
        var res = new ArrayList<Integer>();
        res.add(encodePair(rule.charAt(0), rule.charAt(6)));
        res.add(encodePair(rule.charAt(6), rule.charAt(1)));
        return res;
    }

    private static class Polimerization {
        public Polimerization(int[] initialPairs, Map<Integer, List<Integer>> rules) {
            Arrays.stream(initialPairs).forEach(p -> storePairCount(p, 1L));
            this.rules = rules;
        }

        public void step() {
            Map<Integer, Long> counts = pairCounts;
            pairCounts = new TreeMap<>();
            for (Map.Entry<Integer, Long> pair : counts.entrySet()) {
                var newPairs = rules.getOrDefault(pair.getKey(), List.of(pair.getKey()));
                for (var n : newPairs) {
                    storePairCount(n, pair.getValue());
                }
            }
        }

        public Map<Character, Long> getFrequencies() {
            Map<Character, Long> res = new TreeMap<>();
            for (Map.Entry<Integer, Long> e : pairCounts.entrySet()) {
                var key = decodePair(e.getKey());
                res.put(key.charAt(0), res.getOrDefault(key.charAt(0), 0L) + e.getValue());
                res.put(key.charAt(1), res.getOrDefault(key.charAt(1), 0L) + e.getValue());
            }
            // As we store the overlapping pairs, each letter (*) is counted twice. Fix it
            // up by halving the numbers ((*) rounding up to make up for the first and last letter, which are in fact
            // only stored once)
            for (Map.Entry<Character, Long> e : res.entrySet()) {
                res.put(e.getKey(), (e.getValue() + 1) / 2);
            }
            return res;
        }

        private void storePairCount(int pair, Long count) {
            pairCounts.put(pair, pairCounts.getOrDefault(pair, 0L) + count);
        }

        private final Map<Integer, List<Integer>> rules;
        private Map<Integer, Long> pairCounts = new TreeMap<Integer, Long>();
    }
    @Override
    public long runPartOne() {
        var poly = new Polimerization(initialPairs, rules);
        for (int i = 0; i < 10; i++) {
            poly.step();
        }
        var stats = poly.getFrequencies().values().stream().collect(Collectors.summarizingLong(Long::longValue));
        return stats.getMax() - stats.getMin();
    }

    @Override
    public long runPartTwo() {
        var poly = new Polimerization(initialPairs, rules);
        for (int i = 0; i < 40; i++) {
            poly.step();
        }
        var stats = poly.getFrequencies().values().stream().collect(Collectors.summarizingLong(Long::longValue));
        return stats.getMax() - stats.getMin();
    }
}
