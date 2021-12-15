package com.company;

import java.util.List;

abstract class Puzzle {
    public Puzzle(List<String> lines) {
        this.lines = lines;
    }

    public abstract long runPartOne();
    public abstract long runPartTwo();

    protected List<String> lines;
}
