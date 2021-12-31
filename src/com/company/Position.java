package com.company;

import java.util.Objects;

class Position implements Comparable<Position> {
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Position parseString(String s) {
        var parts = s.split(",", 2);
        return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private final int x;
    private final int y;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Position o) {
        if (this.y == o.y) {
            return Integer.compare(this.x, o.x);
        }
        return Integer.compare(this.y, o.y);
    }
}
