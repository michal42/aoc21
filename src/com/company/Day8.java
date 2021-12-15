package com.company;

import java.util.*;
import java.util.stream.Collectors;

public class Day8 extends Puzzle {
    private final InputLine[] parsedlines;

    public Day8(List<String> lines) {
        super(lines);
        parsedlines = lines.stream().map(InputLine::new).toArray(InputLine[]::new);
    }

    public static int countBits(int n) {
        int res = 0;
        while (n > 0) {
            n &= n - 1;
            res++;
        }
        return res;
    }

    public class Digit {
        private byte segments;

        public String getOriginalCode() {
            return originalCode;
        }

        private final String originalCode;

        public Digit(String code) {
            originalCode = Arrays.stream(code.split("")).sorted().collect(Collectors.joining());
            segments = 0;
            for (var c : code.getBytes()) {
                if (c >= 'a' && c <= 'g') {
                    segments |= 1 << (c - 'a');
                }
            }
        }

        public int numSegments() {
            return countBits(segments);
        }

        public byte getCode() {
            return segments;
        }
    }

    public class InputLine {
        private Digit[] outputDigits;
        Map<Byte, Integer> codeToValue = new TreeMap<>();
        Map<Integer, Byte> valueToCode = new TreeMap<>();

        private void mapDigit(Digit d, int value) {
            codeToValue.put(d.getCode(), value);
            valueToCode.put(value, d.getCode());
        }

        public InputLine(String str) {
            var sides = str.split("\\s*\\|\\s*");
            outputDigits = Arrays.stream(sides[1].split("\\s+")).map(Digit::new).toArray(Digit[]::new);
            var combinations = Arrays.stream(sides[0].split("\\s+")).map(Digit::new).toArray(Digit[]::new);
            List<Digit> candidates235 = new ArrayList<>();
            List<Digit> candidates069 = new ArrayList<>();
            for (var d : combinations) {
                switch (d.numSegments()) {
                    case 2:
                        mapDigit(d, 1);
                        break;
                    case 3:
                        mapDigit(d, 7);
                        break;
                    case 4:
                        mapDigit(d, 4);
                        break;
                    case 5:
                        candidates235.add(d);
                        break;
                    case 6:
                        candidates069.add(d);
                        break;
                    case 7:
                        mapDigit(d, 8);
                }
            }
            for (var d : candidates235) {
                if ((d.getCode() & valueToCode.get(1)) == valueToCode.get(1)) {
                    mapDigit(d, 3);
                } else if (countBits(d.getCode() & valueToCode.get(4)) == 3) {
                    mapDigit(d, 5);
                } else {
                    mapDigit(d, 2);
                }
            }
            for (var d : candidates069) {
                if ((d.getCode() & valueToCode.get(3)) == valueToCode.get(3)) {
                    mapDigit(d, 9);
                } else if ((d.getCode() & valueToCode.get(5)) == valueToCode.get(5)) {
                    mapDigit(d, 6);
                } else {
                    mapDigit(d, 0);
                }
            }
            assert (codeToValue.size() == 10);
            assert (valueToCode.size() == 10);
        }

        public int count1478() {
            return (int)(Arrays.stream(outputDigits).filter(d -> { var n = d.numSegments(); return n <= 4 || n == 7; }).count());
        }

        public int getOutputValue() {
            int res = 0;
            for (var d : outputDigits) {
                res *= 10;
                res += codeToValue.get(d.getCode());
            }
            return res;
        }
    }

    @Override
    public long runPartOne() {
        return Arrays.stream(parsedlines).mapToInt(InputLine::count1478).sum();
    }

    @Override
    public long runPartTwo() {
        return Arrays.stream(parsedlines).mapToInt(InputLine::getOutputValue).sum();
    }
}
