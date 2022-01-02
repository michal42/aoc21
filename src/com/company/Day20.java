package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Day20 extends Puzzle {
    public Day20(List<String> lines) {
        super(lines);
        palette = lines.stream().takeWhile(Predicate.not(String::isEmpty)).flatMapToInt(String::chars).map(c -> c == '#' ? 1 : 0).toArray();
        assert palette.length == 512;
        var pixels = lines.stream().dropWhile(Predicate.not(String::isEmpty)).skip(1).map(l -> l.chars().map(c -> c == '#' ? 1 : 0).toArray()).toArray(int[][]::new);
        originalImage = new Image(pixels, 0);
    }

    private final int[] palette;
    private final Image originalImage;

    private class Image {
        public Image(int[][] pixels, int background) {
            this.pixels = pixels;
            this.width = pixels[0].length;
            this.height = pixels.length;
            this.background = background;
        }

        public long countPixels() {
            return Arrays.stream(pixels).mapToLong(a -> Arrays.stream(a).sum()).sum();
        }

        public Image getEnhancedImage() {
            int newWidth = width + 2;
            int newHeight = height + 2;
            int[][] newPixels = new int[newHeight][newWidth];
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    newPixels[y][x] = getEnhancedPixel(x - 1, y - 1);
                }
            }
            int newBackground = getEnhancedPixel(-2, -2);
            return new Image(newPixels, newBackground);
        }

        private int getEnhancedPixel(int x, int y) {
            //    -1   1
            // -1  8 7 6
            //     5 4 3
            //  1  2 1 0
            int code = 0;
            for (int i = 0; i < 9; i++) {
                code |= getPixel(x + 1 - i % 3, y + 1 - i / 3) << i;
            }
            return palette[code];
        }

        private int getPixel(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= width) {
                return background;
            }
            return pixels[y][x];
        }

        private final int width;
        private final int height;
        private final int[][] pixels;
        private final int background;
    }
    @Override
    public long runPartOne() {
        var image = originalImage.getEnhancedImage();
        image = image.getEnhancedImage();
        return image.countPixels();
    }

    @Override
    public long runPartTwo() {
        var image = originalImage;
        for (int i = 0; i < 50; i++) {
            image = image.getEnhancedImage();
        }
        return image.countPixels();
    }
}
