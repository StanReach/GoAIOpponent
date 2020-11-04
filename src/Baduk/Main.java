package Baduk;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {

    private static final Point PASS = null;
    private static Scanner playerInput;

    private static final Set<GroupOfStones> setOfStoneGroups = new HashSet<>();
    private static final Set<Territory> setOfTerritory = new HashSet<>();
    
    private static int AIPieceColour;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    private static final int EMPTY = 0;
    private static final int boardHeight = 9;
    private static final int boardWidth = 9;
    private static int[][] GOBoard;
    private static int colourToMove = BLACK;
    private static Point lastMove = null;

    public static void main(String[] args) {
        GOBoard = new int[boardWidth][boardHeight];
        playerInput = new Scanner(System.in);
        
        randomlyChoosePieceColour();

        Point move;

        addAllPointsToInitialTerritory();

        printBoard();

        boolean gameOver = false;

        while (!gameOver) {
            if (colourToMove == AIPieceColour) {
                move = findBestMove();
            } else {
                move = getPlayerMove();
            }
            if (move != PASS) {
                placeStone(move);
                updateGroupList(move);
                captureStones();
                updateTerritory(move);
            }
            declareEndOfTurn(move);
            printBoard();
            printScore();
            if (move == PASS && lastMove == PASS) {
                gameOver = true;
            }
            lastMove = move;
        }
        printBoard();
        printScore();
    }

    private static void printScore() {
        System.out.println("Black scored: " + getScoreForColour(BLACK));
        System.out.println("White scored: " + getScoreForColour(WHITE));
    }

    private static int getScoreForColour(int colour) {
        int score = 0;
        for (GroupOfStones x : setOfStoneGroups) {
            if (x.colour == colour) {
                score += x.stonesInGroup.size();
            }
        }
        for (Territory x : setOfTerritory) {
            if (x.owner == colour) {
                score += x.territory.size();
            }
        }
        return score;
    }

    private static void printSets() {
        for(GroupOfStones x : setOfStoneGroups){
            System.out.println("Stones in group: " + x.stonesInGroup);
            System.out.println("liberties in group: " + x.liberties);
            System.out.println();
        }
        for(Territory x : setOfTerritory){
            System.out.println("Owner of Territory is: " + x.owner + " Spaces in territory: " + x.territory);
            System.out.println();
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
        setOfTerritory.remove(getTerritoryThatSpaceIsMemberOf(move));

        for (Point adjacent : pointsAdjacentTo(move)) {
            if (pieceAtPosition(adjacent) == EMPTY && getTerritoryThatSpaceIsMemberOf(adjacent) == null) {
                Territory newTerritory = new Territory(EMPTY);
                newTerritory.territory.add(adjacent);
                setOfTerritory.add(newTerritory);
                floodTerritory(adjacent, newTerritory);
                if (newTerritory.borderingColours.size() == 2) {
                    newTerritory.owner = EMPTY;
                } else {
                    for (Integer x : newTerritory.borderingColours) newTerritory.owner = x;
                }
            }
        }
    }

    private static void floodTerritory(Point point, Territory territory) {

        for (Point adjacent : pointsAdjacentTo(point)) {
            if (pieceAtPosition(adjacent) == EMPTY && !territory.territory.contains(adjacent)) {
                territory.territory.add(adjacent);
                floodTerritory(adjacent,territory);
            } else if (pieceAtPosition(adjacent) != EMPTY) {
                territory.borderingColours.add(pieceAtPosition(adjacent));
            }
        }
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
        System.out.println();
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
            System.out.println("The player placed a stone at position " + point);
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
        GroupOfStones groupToBeRemoved = null;

        for (GroupOfStones group : setOfStoneGroups) {
            if (group.stonesInGroup.contains(unionPoint)) {
                newGroup.stonesInGroup.addAll(group.stonesInGroup);
                newGroup.liberties.addAll(group.liberties);
                newGroup.liberties.remove(move);
                newGroup.liberties.remove(unionPoint);
                groupToBeRemoved = group;
            }
        }
        setOfStoneGroups.remove(groupToBeRemoved);
        setOfStoneGroups.removeAll(Collections.EMPTY_SET);
        return newGroup;
    }

    private static Point findBestMove() {
        List<Point> legalPositions = getListOfLegalPositions();

        if (legalPositions.size() == 0) {
            return PASS;
        } else {
            int randomIndex = (int) (Math.random() * (legalPositions.size()));
            return legalPositions.get(randomIndex);
        }
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
            if (colourToMove != AIPieceColour) {
                System.out.println("position was out of bounds, try again");
            }
            return false;
        }
        if (isOccupied(point)) {
            if (colourToMove != AIPieceColour) {
                System.out.println("position was occupied, try again");
            }
            return false;
        }
        if (isSuicide(point)) {
            if (colourToMove != AIPieceColour) {
                System.out.println("position was suicidal, try again");
            }
            return false;
        }
        if (isKo(point)) {
            if (colourToMove != AIPieceColour) {
                System.out.println("position was KO, try again");
            }
            return false;
        }
        return true;
    }

    private static boolean isKo(Point point) {
        for (Point adjacentPoint : pointsAdjacentTo(point)) {
            if (adjacentPoint.equals(lastMove)) {
                if (getGroupThatStoneIsMemberOf(adjacentPoint).liberties.size() == 1) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private static Territory getTerritoryThatSpaceIsMemberOf(Point space) {
        for (Territory territory : setOfTerritory) {
            if (territory.territory.contains(space)) {
                return territory;
            }
        }
        return null;
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
                break;
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
        if (createLiberties) {
            return false;
        }
        return !hasLiberties;
    }

    private static int oppositeColour() {
        if (colourToMove == BLACK) {
            return WHITE;
        }
        return BLACK;
    }

    private static boolean isOccupied(Point point) {
        return GOBoard[point.x][point.y] != 0;
    }

    private static boolean isOutOfBounds(Point point) {
        if (boardWidth <= point.x || point.x < 0) {
            return true;
        }
        return boardHeight <= point.y || point.y < 0;
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
