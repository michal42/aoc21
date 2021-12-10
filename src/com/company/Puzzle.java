package com.company;

import java.util.List;

abstract class Puzzle {
    public Puzzle(List<String> lines) {
        this.lines = lines;
    }
    public abstract int runIt();

    protected List<String> lines;
}
