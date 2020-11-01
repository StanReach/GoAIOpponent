package Baduk;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class GroupOfStones {
    int colour;

    Set<Point> stonesInGroup;
    Set<Point> liberties;

    GroupOfStones(int setColour) {
        colour = setColour;
        stonesInGroup = new HashSet<Point>();
        liberties = new HashSet<Point>();
    }
}
