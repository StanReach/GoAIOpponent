package Baduk;

public class Main {
    
    private static Boolean gameOver = false;
    
    private static int AIPieceColour;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
            
    public static void main(String[] args) {
        int colourToMove = BLACK;
        
        randomlyChoosePieceColour();
        
        while (!gameOver) {
            if (colourToMove == AIPieceColour) {
                findBestMove();
                placeStone();
            } else {
                getPlayerMove();
            }
            captureStones();
            declareEndOfTurn();
        }
    }

    private static void declareEndOfTurn() {
    }

    private static void getPlayerMove() {
    }

    private static void captureStones() {
    }

    private static void placeStone() {
    }

    private static void findBestMove() {
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
