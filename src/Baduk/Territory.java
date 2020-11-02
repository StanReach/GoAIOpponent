package Baduk;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Territory {
    int owner;

    Set<Point> territory;
    Set<Integer> borderingColours;

    Territory(int setColour) {
        owner = setColour;
        territory = new HashSet<Point>();
        borderingColours = new HashSet<Integer>();
    }
}
