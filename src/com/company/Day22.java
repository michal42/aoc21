package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Day22 extends Puzzle {
    public Day22(List<String> lines) {
        super(lines);

        cuboids = lines.stream().map(Cuboid::parseString).toArray(Cuboid[]::new);
    }

    private final Cuboid[] cuboids;

    private record Cuboid(int x1, int y1, int z1, int x2, int y2, int z2, boolean on) {
        static public Cuboid createIfValid(int x1, int y1, int z1, int x2, int y2, int z2, boolean on) {
            if (x1 > x2 || y1 > y2 || z1 > z2) {
                return null;
            }
            return new Cuboid(x1, y1, z1, x2, y2, z2, on);
        }

        private static final Pattern specRe = Pattern.compile(
                "^(on|off)\\s+x=(-?\\d+)\\.\\.(-?\\d+),\s*y=(-?\\d+)\\.\\.(-?\\d+),\s*z=(-?\\d+)\\.\\.(-?\\d+)");

        static public Cuboid parseString(String spec) {
            var match = specRe.matcher(spec);
            if (!match.find()) {
                return null;
            }
            boolean on = "on".equals(match.group(1));
            int x1 = Integer.parseInt(match.group(2));
            int x2 = Integer.parseInt(match.group(3));
            int y1 = Integer.parseInt(match.group(4));
            int y2 = Integer.parseInt(match.group(5));
            int z1 = Integer.parseInt(match.group(6));
            int z2 = Integer.parseInt(match.group(7));
            return createIfValid(x1, y1, z1, x2, y2, z2, on);
        }

        public long volume() {
            return (long) (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
        }

        public boolean intersects(Cuboid o) {
            // If the two cuboids intersect, all their three projections must intersect
            return x2 >= o.x1 && o.x2 >= x1 &&
                   y2 >= o.y1 && o.y2 >= y1 &&
                   z2 >= o.z1 && o.z2 >= z1;
        }

        public List<Cuboid> subtract(Cuboid o) {
            // Assuming that o intersects with this, return up to six disjoint cuboids that form the result
            // of subtracting o from this. Within o.z1..o.z2, there will be up to four shapes like this:
            //
            //      x1  o.x1 o.x2  x2
            //   y1 aaaaaaaaaaaaa bbb
            //      aaaaaaaaaaaaa bbb
            // o.y1 ddd ooooooooo bbb
            //      ddd ooooooooo bbb
            // o.y2 ddd ooooooooo bbb
            //      ddd ccccccccccccc
            // y2   ddd ccccccccccccc
            //
            // plus up to two cuboids under o.z1 (e) and above o.z2 (f), respectively

            // Make sure we don't extend the resulting cuboids beyond the original
            int ox1 = Math.max(x1, o.x1);
            int oy1 = Math.max(y1, o.y1);
            int oz1 = Math.max(z1, o.z1);
            int ox2 = Math.min(x2, o.x2);
            int oy2 = Math.min(y2, o.y2);
            int oz2 = Math.min(z2, o.z2);
            List<Cuboid> res = new ArrayList<>();
            // a
            res.add(createIfValid(x1,      y1,      oz1, ox2,     oy1 - 1, oz2, on));
            // b
            res.add(createIfValid(ox2 + 1, y1,      oz1, x2,      oy2,     oz2, on));
            // c
            res.add(createIfValid(ox1,     oy2 + 1, oz1, x2,      y2,      oz2, on));
            // d
            res.add(createIfValid(x1,      oy1,     oz1, ox1 - 1, y2,      oz2, on));
            // e
            res.add(createIfValid(x1, y1, z1, x2, y2, oz1 - 1, on));
            // f
            res.add(createIfValid(x1, y1, oz2 + 1, x2, y2, z2, on));
            return res.stream().filter(Objects::nonNull).toList();
        }
    }

    private static class Reactor {
        public void addCuboid(Cuboid newCuboid) {
            List<Cuboid> splitCuboids = new ArrayList<>();
            var it = cuboidsOn.listIterator();
            while (it.hasNext()) {
                var existingCuboid = it.next();
                if (!existingCuboid.intersects(newCuboid)) {
                    continue;
                }
                it.remove();
                splitCuboids.addAll(existingCuboid.subtract(newCuboid));
            }
            cuboidsOn.addAll(splitCuboids);
            if (newCuboid.on()) {
                cuboidsOn.add(newCuboid);
            }
        }

        public long getOnCount() {
            return cuboidsOn.stream().mapToLong(Cuboid::volume).sum();
        }

        private final List<Cuboid> cuboidsOn = new LinkedList<>();
    }

    @Override
    public long runPartOne() {
        var reactor = new Reactor();
        // The large cuboids in the input conveniently do not touch the inner 101x101x101 cube
        Arrays.stream(cuboids).filter(c -> Math.abs(c.x1()) <= 50).forEach(reactor::addCuboid);
        return reactor.getOnCount();
    }

    @Override
    public long runPartTwo() {
        var reactor = new Reactor();
        Arrays.stream(cuboids).forEach(reactor::addCuboid);
        return reactor.getOnCount();
    }
}
