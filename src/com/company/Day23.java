package com.company;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day23 extends Puzzle {
    public Day23(List<String> lines) {
        super(lines);
    }

    private static class State {
        //  The fields are numbered as follows
        //  hallway:  0  1  2  3  4  5  6  7  8  9 10
        //  rooms:         20    40    60    80
        //                 21    41    61    81
        //                 22    42    62    82
        //                 23    43    63    83
        //
        // The numbering of the amphipods (their index in the positions array) is A: 0-1, B: 2-3, C: 4-5, D: 6-7
        public static final int AMPHIPOD_KINDS = 4;
        public final int numAmphipods;
        public final int roomSize;
        private static final int HALLWAY_START = 0;
        private static final int HALLWAY_END = 10;

        private static boolean isRoom(int field) {
            return field > HALLWAY_END;
        }

        private static boolean isEntrance(int field) {
            return field >= 2 && field <= 8 && field % 2 == 0;
        }

        private static int roomNumber(int field) {
            return field - field % 20;
        }

        private static int roomEntrance(int field) {
            return field / 10;
        }

        public int amphipodHome(int amphipod) {
            return (amphipod / roomSize + 1) * 20;
        }

        private int amphipodCost(int amphipod) {
            return switch (amphipod / roomSize) {
                case 0 -> 1;
                case 1 -> 10;
                case 2 -> 100;
                case 3 -> 1000;
                default -> 0;
            };
        }

        private boolean sameAmphipod(int amphipod1, int amphipod2) {
            return amphipod1 / roomSize == amphipod2 / roomSize;
        }

        public State(int[] amphipodPositions) {
            numAmphipods = amphipodPositions.length;
            roomSize = numAmphipods / AMPHIPOD_KINDS;
            assert numAmphipods % AMPHIPOD_KINDS == 0;
            assert roomSize <= 20;
            fieldTenant = new int[80 + roomSize];
            Arrays.fill(fieldTenant, -1);
            this.amphipodPositions = IntStream.range(0, AMPHIPOD_KINDS).flatMap(
                    // Sort the positions within each amphipod kind so that equivalent states are equal
                    i -> Arrays.stream(amphipodPositions).skip(i * roomSize).limit(roomSize).sorted()).toArray();
            for (int a = 0; a < numAmphipods; a++) {
                fieldTenant[amphipodPositions[a]] = a;

            }
        }

        private final int[] amphipodPositions;
        private final int[] fieldTenant;

        public State getFinalState() {
            return new State(IntStream.range(0, numAmphipods).map(a -> amphipodHome(a) + a % roomSize).toArray());
        }

        // State -> cost
        public Map<State, Integer> getAvailableNextStates() {
            Map<State, Integer> res = new HashMap<>();
            for (int amphipod = 0; amphipod < numAmphipods; amphipod++) {
                var moves = getAvailableMovesForAmphipod(amphipod);
                for (var move : moves.entrySet()) {
                    var newPositions = amphipodPositions.clone();
                    newPositions[amphipod] = move.getKey();
                    res.put(new State(newPositions), amphipodCost(amphipod) * move.getValue());
                }
            }
            return res;
        }

        private boolean fieldOccupied(int field) {
            return fieldTenant[field] >= 0;
        }

        private boolean isHome(int amphipod) {
            int field = amphipodPositions[amphipod];
            if (!isRoom(field)) {
                return false;
            }
            int home = amphipodHome(amphipod);
            if (roomNumber(field) != home) {
                return false;
            }
            for (field++; field < home + roomSize; field++) {
                if (!sameAmphipod(amphipod, fieldTenant[field])) {
                    return false;
                }
            }
            return true;
        }

        // Field -> distance
        private Map<Integer, Integer> getAvailableMovesForAmphipod(final int amphipod) {
            // If we are already in the home position, don't move
            if (isHome(amphipod)) {
                return Collections.emptyMap();
            }
            final int field = amphipodPositions[amphipod];
            if (isRoom(field)) {
                // if we start in the lower field of the room and the upper field is full, we can't move
                for (int above = roomNumber(field); above < field; above++) {
                    if (fieldOccupied(above)) {
                        return Collections.emptyMap();
                    }
                }
                int entrance = roomEntrance(field);
                // distance from the room to the entrance
                int toEntrance = field % roomSize + 1;
                int minLeft = entrance;
                int maxRight = entrance;
                while (minLeft >= HALLWAY_START && !fieldOccupied(minLeft) ) {
                    minLeft--;
                }
                while (maxRight <= HALLWAY_END && !fieldOccupied(maxRight)) {
                    maxRight++;
                }
                Map<Integer, Integer> res = new TreeMap<>();
                for (int newField = minLeft + 1; newField < maxRight; newField++) {
                    if (isEntrance(newField)) {
                        continue;
                    }
                    res.put(newField, toEntrance + Math.abs(newField - entrance));
                }
                return res;
            } else {
                int room = amphipodHome(amphipod);
                int targetField = room;
                // Check if there is any foreigh amphipod in the room
                for (int i = room; i < room + roomSize; i++) {
                    if (!fieldOccupied(i)) {
                        targetField = i;
                    } else {
                        if (!sameAmphipod(amphipod, fieldTenant[i])) {
                            return Collections.emptyMap();
                        }
                    }
                }
                int entrance = roomEntrance(room);
                // Check if the path is clear. The entrance is always empty and the starting field doesn't need to be checked
                for (int path = Math.min(entrance, field) + 1; path < Math.max(entrance, field); path++) {
                    if (fieldOccupied(path)) {
                        return Collections.emptyMap();
                    }
                }
                int distance = Math.abs(field - entrance) + (targetField - room + 1);
                return Map.of(targetField, distance);
            }
        }

        private static Pattern inputRe = Pattern.compile("#([A-D])#([A-D])#([A-D])#([A-D])#");
        public static State parseText(List<String> lines) {
            var positionsPerKind = IntStream.range(0, AMPHIPOD_KINDS).mapToObj(i -> new ArrayList<Integer>()).toList();
            int row = 0;
            for (var line : lines ) {
                var matcher = inputRe.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                for (int g = 1; g <= AMPHIPOD_KINDS; g++) {
                    var letter = matcher.group(g);
                    int kind = (letter.charAt(0) - 'A');
                    int field = g * 20 + row;
                    positionsPerKind.get(kind).add(field);
                }
                row++;
            }
            return new State(positionsPerKind.stream().flatMap(List::stream).mapToInt(Integer::intValue).toArray());
        }

        @Override
        public boolean equals(Object o) {
            State state = (State) o;
            return Arrays.equals(amphipodPositions, state == null ? null : state.amphipodPositions);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(amphipodPositions);
        }

        @Override
        public String toString() {
            return Arrays.toString(amphipodPositions);
        }

    }

    private static long findLowestCost(State from, State to) {
        // State -> cost
        Map<State, Long> costs = new HashMap<>();
        long lowestTotalCost = Long.MAX_VALUE;
        Queue<State> queue =  new ArrayDeque<>();
        queue.add(from);
        costs.put(from, 0L);
        while (!queue.isEmpty()) {
            var state = queue.remove();
            var cost = costs.get(state);
            for (var nextMove : state.getAvailableNextStates().entrySet()) {
                var nextState = nextMove.getKey();
                var nextCost = cost + nextMove.getValue();
                if (nextCost >= lowestTotalCost || nextCost >= costs.getOrDefault(nextState, Long.MAX_VALUE)) {
                    continue;
                }
                if (nextState.equals(to)) {
                    lowestTotalCost = nextCost;
                } else {
                    costs.put(nextState, nextCost);
                    queue.add(nextState);
                }
            }
        }
        return lowestTotalCost;
    }

    @Override
    public long runPartOne() {
        var initialState = State.parseText(lines);
        return findLowestCost(initialState, initialState.getFinalState());
    }

    @Override
    public long runPartTwo() {
        var text = lines.stream().dropWhile(String::isEmpty).collect(Collectors.toList());
        text.add(3, "  #D#C#B#A#");
        text.add(4, "  #D#B#A#C#");
        var initialState = State.parseText(text);
        return findLowestCost(initialState, initialState.getFinalState());
    }
}
