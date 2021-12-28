package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> input = new ArrayList<>();
        String line;
        try {
            var br = new BufferedReader(new InputStreamReader(System.in));
            while ((line = br.readLine()) != null) {
                input.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Puzzle p = new Day14(input);
        System.out.println(p.runPartOne());
        System.out.println(p.runPartTwo());
    }
}
