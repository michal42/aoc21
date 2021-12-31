package com.company;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day17 extends Puzzle {
    public Day17(List<String> lines) {
        super(lines);

        Pattern re = Pattern.compile("x=(-?\\d+)\\.\\.(-?\\d+),.*y=(-?\\d+)\\.\\.(-?\\d+)");
        var match = re.matcher(lines.get(0));
        if (!match.find()) {
            throw new IllegalArgumentException("Unexpected input: " + lines.get(0));
        }
        leftX = Integer.parseInt(match.group(1));
        rightX = Integer.parseInt(match.group(2));
        bottomY = Integer.parseInt(match.group(3));
        topY = Integer.parseInt(match.group(4));
        stableHorizontalVMin = getStableHorizontalVMin();
        stableHorizontalVMax = getStableHorizontalVMax();
    }

    private final int leftX, rightX, bottomY, topY;
    private final int stableHorizontalVMin, stableHorizontalVMax;

    private static int ceilDiv(int x, int y) {
        return - Math.floorDiv(-x, y);
    }

    private List<Integer> verticalVforNSteps(int steps)
    {
        // For a given vertical V, the position after N steps is
        // V * N            - N * (N - 1) / 2
        // ^linear movement   ^ free fall
        // We must find such values of V that satisfy
        // bottomY <=  V * N - freeFall / 2 <= topY
        // bottomY + freeFall <= V * N <= topY + freeFall
        // (bottomY + freeFall) / N <= V <= (topY + freeFall) / N
        int freeFall = steps * (steps - 1) / 2;
        int minV = ceilDiv(bottomY + freeFall, steps);
        int maxV = Math.floorDiv(topY + freeFall, steps);
        return IntStream.rangeClosed(minV, maxV).boxed().toList();
    }

    private int getStableHorizontalVMin()
    {
        // V * (V + 1) / 2 >= leftX
        // 1/2 * V^2 + 1/2 * V - leftX >= 0
        // V^2 + V - 2 * leftX >= 0
        // V >= (-1 + sqrt(1 + 8 * leftX)) / 2  (the other solution of the quadratic formula is always negative)
        return (int)Math.ceil((-1 + Math.sqrt(1 + 8 * leftX)) / 2);
    }

    private int getStableHorizontalVMax()
    {
        // See the comment in getStableHorizontalVMin
        return (int)Math.floor((-1 + Math.sqrt(1 + 8 * rightX)) / 2);
    }
    private List<Integer> horizontalVforNSteps(int steps)
    {
        // For a given horizontal V, the position after N steps is
        // V * N             - N * (N - 1) / 2   for V >= N
        // ^linear movement    ^ drag
        //
        // V * V             - V * (V - 1) / 2   for V < N
        // V * (V + 1) / 2
        // For the latter case, we simply find the min and max speeds whose trajectories converge within the target area
        int drag = steps * (steps - 1) / 2;
        int minV = steps >= stableHorizontalVMin ? stableHorizontalVMin :
                ceilDiv(leftX + drag, steps);
        int maxV = steps >= stableHorizontalVMax ? stableHorizontalVMax :
                Math.floorDiv(rightX + drag, steps);

        return IntStream.rangeClosed(minV, maxV).boxed().toList();
    }

    @Override
    public long runPartOne() {
        // The trick is to reach the bottom of the target are after reaching zero. This assumes that the area is fully
        // below 0 and that it is not too far and too narrow
        var verticalV = -bottomY - 1;
        return verticalV * (verticalV + 1 ) / 2;
    }

    @Override
    public long runPartTwo() {
        // See part one
        var maxSteps = -bottomY * 2;
        // Some speed's trajectories hit the target area more than once, hence the set
        Set<Position> unique = new TreeSet<>();
        for (int n = 1; n <= maxSteps; n++) {
            var horizontalVs = horizontalVforNSteps(n);
            var verticalVs = verticalVforNSteps(n);
            horizontalVs.forEach(x -> { verticalVs.forEach(y -> unique.add(new Position(x, y)));});
        }
        return unique.size();
    }
}
