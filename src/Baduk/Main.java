package Baduk;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {

    private static Scanner playerInput;
    
    private static Boolean gameOver = false;

    private static final Set<GroupOfStones> setOfStoneGroups = new HashSet<>();
    private static final Set<Territory> setOfTerritory = new HashSet<>();
    
    private static int AIPieceColour;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    private static final int EMPTY = 0;
    private static final int boardHeight = 4;
    private static final int boardWidth = 4;
    private static int[][] GOBoard;
    private static int colourToMove = BLACK;

    public static void main(String[] args) {
        GOBoard = new int[boardWidth][boardHeight];
        playerInput = new Scanner(System.in);
        
        randomlyChoosePieceColour();

        Point move;

        addAllPointsToInitialTerritory();
        
        while (!gameOver) {
            if (colourToMove == AIPieceColour) {
                move = findBestMove();
            } else {
                move = getPlayerMove();
            }
            placeStone(move);
            updateGroupList(move);
            captureStones();
            updateTerritory(move);
            declareEndOfTurn(move);
            for(GroupOfStones x : setOfStoneGroups){
                System.out.println("Stones in group: " + x.stonesInGroup);
                System.out.println("liberties in group: " + x.liberties);
                System.out.println();
            }
            for(Territory x : setOfTerritory){
                System.out.println("Spaces in territory: " + x.territory);
                System.out.println();
            }
            printBoard();
        }
    }

    private static List<Point> pointsAdjacentTo(Point point) {
        List<Point> pointsAdjacentToPoint = new ArrayList<>();

        if (point.x + 1 < boardWidth) {
            pointsAdjacentToPoint.add(new Point(point.x + 1, point.y));
        }
        if (point.x - 1 >= 0) {
            pointsAdjacentToPoint.add(new Point(point.x - 1, point.y));
        }
        if (point.y + 1 < boardHeight) {
            pointsAdjacentToPoint.add(new Point(point.x, point.y + 1));
        }
        if (point.y - 1 >= 0) {
            pointsAdjacentToPoint.add(new Point(point.x, point.y - 1));
        }
        return pointsAdjacentToPoint;
    }

    private static void updateTerritory(Point move) {
        for (Territory x : setOfTerritory) {
            if (x.territory.contains(move)) {
                setOfTerritory.remove(x);
            }
        }
        
        if (move.x + 1 < boardWidth && (GOBoard[move.x + 1][move.y] == EMPTY)) {
            Territory newTerritory = floodTerritory(new Point(move.x + 1, move.y));
        }
        if (move.x - 1 >= 0 && (GOBoard[move.x - 1][move.y] == EMPTY)) {

        }
        if (move.y + 1 < boardWidth && (GOBoard[move.x][move.y + 1] == EMPTY)) {

        }
        if (move.y - 1 >= 0 && (GOBoard[move.x][move.y - 1] == EMPTY)) {

        }
    }

    private static Territory floodTerritory(Point point) {
        Territory newTerritory = new Territory(EMPTY);

        while (true) {
            newTerritory.territory.add(point);
            break;
        }
        return newTerritory;
    }

    private static void addAllPointsToInitialTerritory() {
        Territory initialTerritory = new Territory(EMPTY);

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                initialTerritory.territory.add(new Point(x,y));
            }
        }

        setOfTerritory.add(initialTerritory);
    }

    private static void printBoard() {
        System.out.println(Arrays.deepToString(GOBoard).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
        //Code snippet taken from "https://stackoverflow.com/questions/19648240/java-best-way-to-print-2d-array"
    }

    private static void declareEndOfTurn(Point point) {
        if (colourToMove == WHITE) {
            System.out.println("WHITE ENDED TURN!");
            colourToMove = BLACK;
        } else {
            System.out.println("BLACK ENDED TURN!");
            colourToMove = WHITE;
        }
            System.out.println("The player placed a stone at position " + point.x + "," + point.y);
    }

    private static Point getPlayerMove() {
        Point playerMoveCoordinate;
        do {
            System.out.println("please type the position of your placed stone in the format <Number>,<Number> and press enter");
            String playerMoveInput = playerInput.nextLine();
            playerMoveCoordinate = parsePlayerInputToPoint(playerMoveInput);
        } while (!isLegalPosition(playerMoveCoordinate));
        return playerMoveCoordinate;
    }

    private static Point parsePlayerInputToPoint(String playerMoveInput) {
        String[] coords = playerMoveInput.split(",");
        System.out.println(coords[0] + " " + coords[1]);
        return (new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
    }



    private static void captureStones() {
        Set<GroupOfStones> groupsThatAreCaptured = new HashSet<>();

        for (GroupOfStones groups : setOfStoneGroups) {
            if (groups.liberties.size() == 0 && groups.colour != colourToMove) {
                for (Point stone : groups.stonesInGroup) {
                    returnStonesLibertiesToGroups(stone);
                }
                groupsThatAreCaptured.add(groups);
            }
        }
        setOfStoneGroups.removeAll(groupsThatAreCaptured);
    }

    public static int pieceAtPosition(Point position) {
        return GOBoard[position.x][position.y];
    }

    static void returnStonesLibertiesToGroups(Point stone) {
        GOBoard[stone.x][stone.y] = EMPTY;

        for (Point adjacentStone : pointsAdjacentTo(stone)) {
            if (pieceAtPosition(adjacentStone) != EMPTY) {
                getGroupThatStoneIsMemberOf(adjacentStone).liberties.add(stone);
            }
        }
    }

    private static void placeStone(Point move) {
        GOBoard[move.x][move.y] = colourToMove;
    }

    private static void updateGroupList(Point move) {
        GroupOfStones newGroup = new GroupOfStones(colourToMove);
        newGroup.stonesInGroup.add(move);
        
        for (Point adjacentPoint : pointsAdjacentTo(move)) {
            if (pieceAtPosition(adjacentPoint) == EMPTY) {
                newGroup.liberties.add(adjacentPoint);
            } else if (pieceAtPosition(adjacentPoint) == colourToMove) {
                newGroup = deleteObseleteGroupAndReturnMergedGroup(newGroup, adjacentPoint, move);
            }
        }

        removeLibertysFromOpponentsGroup(move);
        setOfStoneGroups.add(newGroup);
    }

    private static void removeLibertysFromOpponentsGroup(Point move) {
        for (GroupOfStones groups : setOfStoneGroups) {
            groups.liberties.remove(move);
        }
    }

    private static GroupOfStones deleteObseleteGroupAndReturnMergedGroup(GroupOfStones newGroup, Point unionPoint, Point move) {
        GroupOfStones mergedGroup = new GroupOfStones(colourToMove);
        GroupOfStones groupToBeRemoved = null;

        for (GroupOfStones group : setOfStoneGroups) {
            if (group.stonesInGroup.contains(unionPoint)) {
                mergedGroup.stonesInGroup.addAll(group.stonesInGroup);
                mergedGroup.stonesInGroup.addAll(newGroup.stonesInGroup);
                mergedGroup.liberties.addAll(group.liberties);
                mergedGroup.liberties.addAll(newGroup.liberties);
                mergedGroup.liberties.remove(move);
                mergedGroup.liberties.remove(unionPoint);
                groupToBeRemoved = group;
            }
        }
        setOfStoneGroups.remove(groupToBeRemoved);
        setOfStoneGroups.removeAll(Collections.EMPTY_SET);
        return mergedGroup;
    }

    private static Point findBestMove() {
        List<Point> legalPositions = getListOfLegalPositions();
        //System.out.println(legalPositions);
        int randomIndex = (int) (Math.random() * (legalPositions.size()));
        return legalPositions.get(randomIndex);
    }

    private static List<Point> getListOfLegalPositions() {
        List<Point> legalPositions = new ArrayList<>();

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (isLegalPosition(new Point(x,y))) {
                    legalPositions.add(new Point(x,y));
                }
            }
        }
        return legalPositions;
    }

    private static boolean isLegalPosition(Point point) {
        if (isOutOfBounds(point)) {
            System.out.println("position was out of bounds, try again");
            return false;
        }
        if (isOccupied(point)) {
            System.out.println("position was occupied, try again");
            return false;
        }
        if (isSuicide(point)) {
            System.out.println("position was suicidal, try again");
            return false;
        }
        //Ko Exception
        return true;
    }

    private static GroupOfStones getGroupThatStoneIsMemberOf(Point stone) {
        for (GroupOfStones group : setOfStoneGroups) {
            if (group.stonesInGroup.contains(stone)) {
                return group;
            }
        }
        return null;
    }

    private static boolean isSuicide(Point move) {
        boolean hasLiberties = false;
        
        for (Point adjacentPoint : pointsAdjacentTo(move)) {
            if (pieceAtPosition(adjacentPoint) == EMPTY) {
                hasLiberties = true;
            }
        }
        boolean createLiberties = false;
        for (Point adjacentPoint : pointsAdjacentTo(move)) {
            if (pieceAtPosition(adjacentPoint) == oppositeColour()) {
                if (getGroupThatStoneIsMemberOf(adjacentPoint).liberties.size() == 1) {
                    return false;
                }        
            } else if (pieceAtPosition(adjacentPoint) == colourToMove) {
                if (!(getGroupThatStoneIsMemberOf(adjacentPoint).liberties.size() == 1 && !hasLiberties)) {
                    createLiberties = true;
                }
            }
        }
        if (createLiberties == true) {
            return false;
        }
        if (!hasLiberties) {
            return true;
        }
        return false;
    }

    private static int oppositeColour() {
        if (colourToMove == BLACK) {
            return WHITE;
        }
        return BLACK;
    }

    private static boolean isOccupied(Point point) {
        if (GOBoard[point.x][point.y] != 0) {
            return true;
        }
        return false;
    }

    private static boolean isOutOfBounds(Point point) {
        if (boardWidth <= point.x || point.x < 0) {
            return true;
        }
        if (boardHeight <= point.y || point.y < 0) {
            return true;
        }
        return false;
    }

    private static void randomlyChoosePieceColour() {
        AIPieceColour = (int) (Math.random() * (WHITE - BLACK + 1) + BLACK);

        if (AIPieceColour == BLACK) {
            System.out.println("Computer plays first!");
        } else {
            System.out.println("Human plays first!");
        }
    }
}
