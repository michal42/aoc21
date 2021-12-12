package com.company;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day9 extends Puzzle {
    public Day9(List<String> lines) {
        super(lines);

        width = lines.get(0).length();
        height  = lines.size();
        heights = new int[height][];
        int y = 0;
        for (var l : lines) {
            heights[y++] = Arrays.stream(l.split("")).mapToInt(Integer::parseInt).toArray();
        }
        colors = new int[height][width];
    }

    private final int[][] heights;
    private final int width;
    private final int height;
    private int[][] colors;
    private Map<Integer, Integer> colorSizes = new TreeMap<>();
    private Map<Integer, Set<Integer>> mergedColors = new TreeMap<>();

    private List<Integer> getNeighbourHeights(int x, int y) {
        List<Integer> res = new ArrayList<>();
        if (x > 0) {
            res.add(heights[y][x - 1]);
        }
        if (x < width - 1) {
            res.add(heights[y][x + 1]);
        }
        if (y > 0) {
            res.add(heights[y - 1][x]);
        }
        if (y < height - 1) {
            res.add(heights[y + 1][x]);
        }
        return res;
    }

    private int getHeight(int x, int y) {
        return heights[y][x];
    }

    private int getColor(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }
        return colors[y][x];
    }

    private void setColor(int x, int y, int color) {
        colors[y][x] = color;
        var size = colorSizes.get(color);
        if (size == null) {
            size = 1;
        } else {
            size = size + 1;
        }
        colorSizes.put(color, size);
    }

    private void mergeColors(int color1, int color2) {
        if (!mergedColors.containsKey(color1)) {
            mergedColors.put(color1, new TreeSet<>());
        }
        if (!mergedColors.containsKey(color2)) {
            mergedColors.put(color2, new TreeSet<>());
        }
        mergedColors.get(color1).add(color2);
        mergedColors.get(color2).add(color1);
    }

    private long visitColor(int color, Set<Integer> availableColors) {
        if (!availableColors.contains(color)) {
            return 0;
        }
        long res = colorSizes.get(color);
        availableColors.remove(color);
        for (var merged : mergedColors.getOrDefault(color, Collections.emptySet())) {
            res += visitColor(merged, availableColors);
        }
        return res;
    }

    @Override
    public long runIt() {
        int maxColor = -1;

        for (int y = 0; y < height; y++) {
            int prevColor = -1;
            for (int x = 0; x < width; x++) {
                if (getHeight(x, y) == 9) {
                    prevColor = -1;
                    setColor(x, y, prevColor);
                    continue;
                }
                var top = getColor(x, y - 1);
                int newColor;
                if (prevColor < 0) {
                    if (top < 0) {
                        newColor = ++maxColor;
                    } else {
                        newColor = top;
                    }
                } else {
                    newColor = prevColor;
                    if (top >= 0 && top != newColor) {
                        mergeColors(top, newColor);
                    }
                }
                setColor(x, y, newColor);
                prevColor = newColor;
            }
        }
        var availColors = IntStream.rangeClosed(0, maxColor).boxed().collect(Collectors.toSet());
        List<Long> sizes = new ArrayList<>();
        for (int c = 0; c <= maxColor; c++) {
            var size = visitColor(c, availColors);
            if (size > 0) {
                sizes.add(size);
            }
        }
        assert availColors.isEmpty();
        return sizes.stream().sorted(Collections.reverseOrder()).limit(3).reduce(1L, Math::multiplyExact);
    }

    public long runItA() {
        int score = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int p = getHeight(x, y);
                boolean isLow = true;
                for (var n : getNeighbourHeights(x, y)) {
                    if (n <= p) {
                        isLow = false;
                        break;
                    }
                }
                if (isLow) {
                    score += p + 1;
                }
            }
        }
        return score;
    }
}
