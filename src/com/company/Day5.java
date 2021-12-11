package com.company;

import java.util.List;
import java.util.stream.Stream;

public class Day5 extends Puzzle {
    public class Line {
        public Line(String spec) {
            var numbers = Stream.of(spec.split("[-, >]+")).mapToInt(s -> Integer.parseInt(s)).toArray();
            assert(numbers.length == 4);
            x1 = numbers[0];
            y1 = numbers[1];
            x2 = numbers[2];
            y2 = numbers[3];
        }

        private final int x1, y1, x2, y2;

        public int getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public int length() {
            return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        }

        public int stepX() {
            return Integer.signum(x2 - x1);
        }

        public int stepY() {
            return Integer.signum(y2 - y1);
        }

        public boolean isRegular() {
            return x1 == x2 || y1 == y2;
        }
    }

    public class SeaMap {
        public static final int WIDTH = 1000;
        public static final int HEIGHT = 1000;

        public void drawLine(Line line) {
            /*if (!line.isRegular()) {
                return;
            }*/
            int x = line.getX1();
            int y = line.getY1();
            for (int i = 0; i <= line.length(); i++) {
                data[x][y]++;
                x += line.stepX();
                y += line.stepY();
            }
        }

        public int countOverlaps() {
            int res = 0;
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (data[x][y] > 1) {
                        res++;
                    }
                }
            }
            return res;
        }

        private int[][] data = new int[WIDTH][HEIGHT];
    }

    SeaMap map = new SeaMap();

    public Day5(List<String> lines) {
        super(lines);
        for (var l : lines) {
            map.drawLine(new Line(l));
        }
    }

    @Override
    public long runIt() {
        return map.countOverlaps();
    }
}
