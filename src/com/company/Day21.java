package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Day21 extends Puzzle {
    public Day21(List<String> lines) {
        super(lines);

        startingPositions = lines.stream().mapToInt(s -> Integer.parseInt(s.replaceFirst(".*starting position: ", ""))).toArray();
    }

    private final int[] startingPositions;

    private static class Dice100 {
        public long get3Rolls() {
            long third = ++state * 3;
            return (third - 2) % 100 + (third -1 ) % 100 + third % 100;
        }

        public long getRollCount() {
            return state * 3;
        }

        private long state = 0;
    }

    private static class Player {
        public Player(long position) {
            this.position = position - 1;
            this.score = 0;
        }

        public long advance(long count) {
            position = (position + count) % 10;
            score += position + 1;
            return score;
        }

        public long getScore() {
            return score;
        }

        private long position;
        private long score;
    }
    @Override
    public long runPartOne() {
        final int TARGET_SCORE = 1000;
        var players = Arrays.stream(startingPositions).mapToObj(Player::new).toArray(Player[]::new);
        var dice = new Dice100();
        int move = 0;
        while (true) {
            if (players[move++ % 2].advance(dice.get3Rolls()) >= TARGET_SCORE) {
                return dice.getRollCount() * players[move % 2].getScore();
            }
        }
    }

    private static class DiracSolve {
        public static final int NUM_FIELDS = 10;
        public static final int TARGET_SCORE = 21;

        public DiracSolve() {
            // Build a list of winning and losing sequences whose _second_ position is in the given field. The first step
            // will depend on each player's starting position
            winningSequences = new TreeMap<>();
            losingSequences = new TreeMap<>();
            for (int secondField = 1; secondField <= NUM_FIELDS; secondField++) {
                winningSequences.put(secondField, new TreeMap<>());
                losingSequences.put(secondField, new TreeMap<>());
                traverse(secondField, secondField, secondField, 1, 0);
            }
        }

        // second field -> rounds -> possibilities
        private final Map<Integer, Map<Integer, Long>> winningSequences;
        private final Map<Integer, Map<Integer, Long>> losingSequences;

        private void traverse(int secondField, int lastField, int score, long possibilities, int rounds) {
            if (score >= TARGET_SCORE) {
                recordNode(secondField, possibilities, rounds, true);
                return;
            } else {
                recordNode(secondField, possibilities, rounds, false);
            }
            for (int nextField = 1; nextField <= NUM_FIELDS; nextField++) {
                int nextPossibilities = possibilitiesForRoll(fieldsDistance(lastField, nextField));
                if (nextPossibilities == 0) {
                    continue;
                }
                traverse(secondField, nextField, score + nextField, possibilities * nextPossibilities, rounds + 1);
            }
        }

        private void recordNode(int secondField, long possibilities, int rounds, boolean isWinning) {
            var map = (isWinning ? winningSequences : losingSequences).get(secondField);
            map.put(rounds, map.getOrDefault(rounds, 0L) + possibilities);
        }

        private static int fieldsDistance(int from, int to) {
            return (to - from + NUM_FIELDS) % NUM_FIELDS;
        }

        // rounds -> possibilities
        public Map<Integer, Long> getSequencesStartingAt(int firstField, boolean wantWinning) {
            var map = (wantWinning ? winningSequences : losingSequences);
            Map<Integer, Long> res = new TreeMap<>();
            for (int secondField = 1; secondField <=  NUM_FIELDS; secondField++) {
                int possibilities = possibilitiesForRoll(fieldsDistance(firstField, secondField));
                if (possibilities == 0) {
                    continue;
                }
                for (var sequence : map.get(secondField).entrySet()) {
                    var rounds = sequence.getKey() + 1;
                    res.put(rounds, res.getOrDefault(rounds, 0L) + sequence.getValue() * possibilities);
                }
            }
            return res;
        }

        private static int possibilitiesForRoll(int roll) {
            return switch(roll) {
                case 3, 9 -> 1; // 1x 111 | 1x 333
                case 4, 8 -> 3; // 3x 112 | 3x 233
                case 5, 7 -> 6; // 3x 113 3x 122 | 3x 133 3x 223
                case 6 -> 7;    // 6x 123 1x 222
                default -> 0;
            };
        }
    }

    @Override
    public long runPartTwo() {
        var dirac = new DiracSolve();
        var p1SeqWin = dirac.getSequencesStartingAt(startingPositions[0], true);
        var p2SeqWin = dirac.getSequencesStartingAt(startingPositions[1], true);
        var p1SeqLose = dirac.getSequencesStartingAt(startingPositions[0], false);
        var p2SeqLose = dirac.getSequencesStartingAt(startingPositions[1], false);

        long p1WinningUniverses = 0;
        for (var rounds : p1SeqWin.keySet()) {
            // For p1 to win in the Nth round, p2 needs to lose N-1 rounds
            var p2LosingUniverses = p2SeqLose.get(rounds - 1);
            p1WinningUniverses += p1SeqWin.get(rounds) * p2LosingUniverses;
        }
        long p2WinningUniverses = 0;
        for (var rounds : p2SeqWin.keySet()) {
            // For p2 to win in the Nth round, p1 needs to lose the same number of rounds
            var p1LosingUniverses = p1SeqLose.get(rounds);
            if (p1LosingUniverses == null) {
                // p1 can't lose after 10 rounds
                continue;
            }
            p2WinningUniverses += p2SeqWin.get(rounds) * p1LosingUniverses;
        }
        return Math.max(p1WinningUniverses, p2WinningUniverses);
    }
}
