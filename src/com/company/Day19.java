package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day19 extends Puzzle {
    public Day19(List<String> lines) {
        super(lines);

        scanners = new ArrayList<>();
        int pos = 0;
        while (pos < lines.size()) {
            var name = lines.get(pos);
            var beacons = lines.stream().skip(pos + 1).filter(Predicate.not(String::isEmpty)).
                    takeWhile(s -> !s.startsWith("--- ")).map(Vector::new).collect(Collectors.toCollection(TreeSet::new));
            pos += beacons.size();
            while (pos < lines.size() && !lines.get(pos).startsWith("--- ")) {
                pos++;
            }
            Scanner s = new Scanner(name, beacons);
            scanners.add(s);
        }
    }

    private final List<Scanner> scanners;

    private record Vector(int x, int y, int z) implements Comparable<Vector> {
        public Vector(String str) {
            this(Arrays.stream(str.split(",")).mapToInt(Integer::parseInt).toArray());
        }

        public Vector(int[] coordinates) {
            this(coordinates[0], coordinates[1], coordinates[2]);
        }

        @Override
        public int compareTo(Vector o) {
            if (this.z == o.z) {
                if (this.y == o.y) {
                    return Integer.compare(this.x, o.x);
                } else {
                    return Integer.compare(this.y, o.y);
                }
            } else {
                return Integer.compare(this.z, o.z);
            }
        }
    }

    private record Matrix(int x1, int x2, int x3, int y1, int y2, int y3, int z1, int z2, int z3) { }

    private record Scanner(String name, SortedSet<Vector> beacons) { }

    private static Vector vectorDiff(Vector lhs, Vector rhs) {
        return new Vector(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z);
    }

    private static Vector vectorSum(Vector lhs, Vector rhs) {
        return new Vector(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z);
    }

    private static int vectorDistance(Vector lhs, Vector rhs) {
        return Math.abs(lhs.x - rhs.x) + Math.abs(lhs.y - rhs.y) + Math.abs(lhs.z - rhs.z);
    }

    private static Vector rotateVector(Vector v, Matrix m) {
        return new Vector(
                v.x * m.x1  +  v.y * m.x2  +  v.z * m.x3,
                v.x * m.y1  +  v.y * m.y2  +  v.z * m.y3,
                v.x * m.z1  +  v.y * m.z2  +  v.z * m.z3
        );
    }

    private static SortedSet<Vector> shiftVectors(Set<Vector> vectors, Vector shift) {
        return vectors.stream().map(v -> vectorSum(v, shift)).collect(Collectors.toCollection(TreeSet::new));
    }

    private static SortedSet<Vector> rotateVectors(Set<Vector> vectors, Matrix rotation) {
        return vectors.stream().map(v -> rotateVector(v, rotation)).collect(Collectors.toCollection(TreeSet::new));
    }

    private static final Matrix[] rotations = {
            // https://www.euclideanspace.com/maths/algebra/matrix/transforms/examples/index.htm
            new Matrix(  1,  0,  0,     0,  1,  0,     0,  0,  1),
            new Matrix(  1,  0,  0,     0,  0, -1,     0,  1,  0),
            new Matrix(  1,  0,  0,     0, -1,  0,     0,  0, -1),
            new Matrix(  1,  0,  0,     0,  0,  1,     0, -1,  0),
            new Matrix(  0, -1,  0,     1,  0,  0,     0,  0,  1),
            new Matrix(  0,  0,  1,     1,  0,  0,     0,  1,  0),
            new Matrix(  0,  1,  0,     1,  0,  0,     0,  0, -1),
            new Matrix(  0,  0, -1,     1,  0,  0,     0, -1,  0),
            new Matrix( -1,  0,  0,     0, -1,  0,     0,  0,  1),
            new Matrix( -1,  0,  0,     0,  0, -1,     0, -1,  0),
            new Matrix( -1,  0,  0,     0,  1,  0,     0,  0, -1),
            new Matrix( -1,  0,  0,     0,  0,  1,     0,  1,  0),
            new Matrix(  0,  1,  0,    -1,  0,  0,     0,  0,  1),
            new Matrix(  0,  0,  1,    -1,  0,  0,     0, -1,  0),
            new Matrix(  0, -1,  0,    -1,  0,  0,     0,  0, -1),
            new Matrix(  0,  0, -1,    -1,  0,  0,     0,  1,  0),
            new Matrix(  0,  0, -1,     0,  1,  0,     1,  0,  0),
            new Matrix(  0,  1,  0,     0,  0,  1,     1,  0,  0),
            new Matrix(  0,  0,  1,     0, -1,  0,     1,  0,  0),
            new Matrix(  0, -1,  0,     0,  0, -1,     1,  0,  0),
            new Matrix(  0,  0, -1,     0, -1,  0,    -1,  0,  0),
            new Matrix(  0, -1,  0,     0,  0,  1,    -1,  0,  0),
            new Matrix(  0,  0,  1,     0,  1,  0,    -1,  0,  0),
            new Matrix(  0,  1,  0,     0,  0, -1,    -1,  0,  0)
    };

    private static class ExploredSpace {
        ExploredSpace(Scanner initialScanner) {
            scanners = new ArrayList<>();
            scannerPositions = new ArrayList<>();
            scanners.add(initialScanner);
            scannerPositions.add(new Vector(0, 0, 0));
        }

        public static final int MINIMUM_MATCH = 12;

        public boolean tryAdd(Scanner candidate) {
            for (var rotation : rotations) {
                var rotatedBeacons = rotateVectors(candidate.beacons(), rotation);
                for (var existing : scanners) {
                    var existingBeacons = existing.beacons();
                    // We can ignore up to MINIMUM_MATCH - 1 beacons from both sets, as at least one pair of matching
                    // beacons will not be among them
                    var shifts = existingBeacons.stream().skip(MINIMUM_MATCH - 1).flatMap(
                            e -> rotatedBeacons.stream().skip(MINIMUM_MATCH - 1).map(r -> vectorDiff(e, r))
                            ).collect(Collectors.toCollection(TreeSet::new));
                    for (var shift : shifts) {
                        final var shiftedBeacons = shiftVectors(rotatedBeacons, shift);
                        if (countCommon(existingBeacons, shiftedBeacons) >= MINIMUM_MATCH) {
                            scanners.add(new Scanner(candidate.name(), shiftedBeacons));
                            scannerPositions.add(shift);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int countBeacons() {
            return scanners.stream().flatMap(s -> s.beacons().stream()).collect(Collectors.toCollection(TreeSet::new)).size();
        }

        public int getMaxScannerDistance() {
            return IntStream.range(0, scannerPositions.size()).flatMap(
                    i1 -> IntStream.range(i1 + 1, scannerPositions.size()).map(
                            i2 -> vectorDistance(scannerPositions.get(i1), scannerPositions.get(i2)))).max().getAsInt();
        }

        private static <T extends Comparable<T>> int countCommon(SortedSet<T> s1, SortedSet<T> s2) {
            // XXX: Could traverse the sorted sets in a single pass
            return (int)s1.stream().filter(s2::contains).count();
        }

        private final List<Scanner> scanners;
        private final List<Vector> scannerPositions;
    }

    private ExploredSpace explored;
    private void explore() {
        if (explored != null) {
            return;
        }
        explored = new ExploredSpace(scanners.get(0));
        var todo = scanners.stream().skip(1).collect(Collectors.toCollection(LinkedList::new));
        boolean removed;
        do {
            removed = false;
            var it = todo.iterator();
            while (it.hasNext()) {
                var scanner = it.next();
                if (explored.tryAdd(scanner)) {
                    it.remove();
                    removed = true;
                }
            }
        } while (removed);
        if (!todo.isEmpty()) {
            System.err.println("Following scanners could not be matched: ");
            todo.forEach(s -> System.out.println(s.name()));
        }
    }
    @Override
    public long runPartOne() {
        explore();
        return explored.countBeacons();
    }

    @Override
    public long runPartTwo() {
        explore();
        return explored.getMaxScannerDistance();
    }
}
