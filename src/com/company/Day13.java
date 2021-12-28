package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day13 extends Puzzle {
    private static class Fold {
        public static final int HORIZONTAL = 0;
        public static final int VERTICAL = 1;

        public Fold(int direction, int foldLine) {
            this.direction = direction;
            this.foldLine = foldLine;
        }

        public static Fold parseString(String s) {
            var parts = s.split("=", 2);
            var where = Integer.parseInt(parts[1]);
            if ("fold along x".equals(parts[0])) {
                return new Fold(VERTICAL, where);
            } else if ("fold along y".equals(parts[0])) {
                return new Fold(HORIZONTAL, where);
            } else {
                throw new IllegalArgumentException("invalid fold instructions");
            }
        }

        public boolean isHorizontal() {
            return direction == HORIZONTAL;
        }

        public boolean isVertical() {
            return direction == VERTICAL;
        }

        public int getFoldLine() {
            return foldLine;
        }

        private final int direction;
        private final int foldLine;
    }

    private static class Dot {
        public Dot(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public static Dot parseString(String s) {
            var parts = s.split(",", 2);
            return new Dot(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        private final int x;
        private final int y;
    }

    private static class Paper {
        public Paper(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void addFold(Fold fold) {
            Map<Integer, Integer> map;
            int limit;
            var foldLine = fold.getFoldLine();
            if (fold.isVertical()) {
                map = mapX;
                limit = width;
                width = foldLine;
            } else {
                map = mapY;
                limit = height;
                height = foldLine;
            }
            for (int line = foldLine + 1; line < limit; line++) {
                map.put(line, 2 * foldLine - line);
            }
        }

        public void addDot(Dot dot) {
            dots.add(dot);
        }

        private int lookupCoord(int coord, Map<Integer, Integer> map) {
            while (map.containsKey(coord)) {
                coord = map.get(coord);
            }
            return coord;
        }

        private int[][] render() {
            var canvas = new int[height][width];
            for (var dot : dots) {
                var x = lookupCoord(dot.getX(), mapX);
                var y = lookupCoord(dot.getY(), mapY);
                canvas[y][x] = 1;
            }
            return canvas;
        }

        public long countDots() {
            var canvas = render();
            return Arrays.stream(canvas).flatMapToInt(Arrays::stream).filter(i -> i > 0).count();
        }

        @Override
        public String toString() {
            var canvas = render();
            return Arrays.stream(canvas).map(
                    a -> Arrays.stream(a).mapToObj(i -> (i > 0 ? "#" : " ")).collect(Collectors.joining())).collect(Collectors.joining("\n"));
        }

        private final List<Dot> dots = new ArrayList<>();
        private Map<Integer, Integer> mapX = new TreeMap<>();
        private Map<Integer, Integer> mapY = new TreeMap<>();
        private int width;
        private int height;
    }

    public Day13(List<String> lines) {
        super(lines);
        dots = lines.stream().takeWhile(Predicate.not(String::isEmpty)).map(Dot::parseString).toList();
        folds = lines.stream().skip(dots.size()).dropWhile(String::isEmpty).map(Fold::parseString).toList();
        paperWidth = 1 + Math.max(dots.stream().mapToInt(Dot::getX).max().orElse(0),
                folds.stream().filter(Fold::isVertical).mapToInt(Fold::getFoldLine).max().orElse(0));
        paperHeight = 1 + Math.max(dots.stream().mapToInt(Dot::getY).max().orElse(0),
                              folds.stream().filter(Fold::isHorizontal).mapToInt(Fold::getFoldLine).max().orElse(0));
    }

    private final List<Dot> dots;
    private final List<Fold> folds;
    private final int paperWidth;
    private final int paperHeight;

    @Override
    public long runPartOne() {
        Paper paper = new Paper(paperWidth, paperHeight);
        dots.forEach(paper::addDot);
        paper.addFold(folds.get(0));
        return paper.countDots();
    }

    @Override
    public long runPartTwo() {
        Paper paper = new Paper(paperWidth, paperHeight);
        dots.forEach(paper::addDot);
        folds.forEach(paper::addFold);
        System.out.println(paper);
        return 0;
    }
}
