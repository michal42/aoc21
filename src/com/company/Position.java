package com.company;

class Position {
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
}
