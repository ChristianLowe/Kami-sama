package com.grognak;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.util.*;

class Game {
    private static final int CPUAI_RANGE = 10;
    private static final int HUMAN_RANGE = 20;
    private static final int MAX_DEPTH = 5;

    private int[][] board;
    private boolean isGameOver;
    private Scanner in;

    Game() {
        init();

        boolean playerTurn;
        System.out.print("Hi, would you like to have the first turn (true/false)? ");
        try {
            playerTurn = in.nextBoolean();
        } catch (InputMismatchException ex) {
            System.out.println("Sorry, I didn't understand your answer. I'll give you the first turn.");
            playerTurn = true;
        }

        System.out.println("\nWelcome! Here is a fresh, new board:");
        while (true) {
            int range = playerTurn ? HUMAN_RANGE : CPUAI_RANGE;
            List<String> validMoves = getValidMoves(range);

            printBoard();

            if (isGameOver || validMoves.isEmpty()) {
                String winner = !playerTurn ? "player" : "computer";
                System.out.printf("Game over! The winner is the %s.\n", winner);
                break;
            }

            if (playerTurn) {
                String move = getPlayerMove(validMoves);
                performMove(move);
            } else {
                computerMoves(validMoves);
            }

            playerTurn = !playerTurn;
        }
    }

    private void computerMoves(List<String> validMoves) {
        int bestScore = Integer.MIN_VALUE;
        String bestMove = null;

        for (String validMove : validMoves) {
            int[][] backupBoard = Arrays.stream(board)
                    .map(int[]::clone)
                    .toArray(int[][]::new);

            performMove(validMove);

            int score = scorePieces(CPUAI_RANGE) - scorePieces(HUMAN_RANGE);
            if (score > bestScore) {
                bestScore = score;
                bestMove = validMove;
            }

            if (isGameOver) {
                return;
            } else {
                board = backupBoard;
            }
        }

        performMove(bestMove);
    }

    /*private int min(int depth) {
        int bestScore = Integer.MAX_VALUE;
        if (depth == MAX_DEPTH) return evaluate();

        for (String validMove : validMoves) {
            int[][] backupBoard = Arrays.stream(board)
                    .map(int[]::clone)
                    .toArray(int[][]::new);

            performMove(validMove);

            int score = min(1);//
            if (score > bestScore) {
                bestScore = score;
                bestMove = validMove;
            }

            if (isGameOver) {
                return;
            } else {
                board = backupBoard;
            }
        }

        return bestScore;
    }*/

    private int evaluate() {
        return scorePieces(CPUAI_RANGE) - scorePieces(HUMAN_RANGE);
    }

    private int scorePieces(int range) {
        int score = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                int pieceType = board[y][x] - range;
                if (pieceType >= 0 && pieceType <= 9) {
                    switch (pieceType) {
                        case 1: // Mini Ninja
                        case 5: // Mini Samurai
                            score += 1;
                            break;
                        case 2: // Norm Ninja
                        case 6: // Norm Samurai
                            score += 3;
                            break;
                        case 9: // The King
                            score += 100;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }
            }
        }

        return score;
    }

    private void performMove(String move) {
        char[] chars = move.toCharArray(); // Algebraic notation
        int x1 = chars[0] - 'A';
        int y1 = 8 - (chars[1] - '0');
        int x2 = chars[2] - 'A';
        int y2 = 8 - (chars[3] - '0');

        performMove(y1, x1, y2, x2);
    }

    private void performMove(int y1, int x1, int y2, int x2) {
        int piece = board[y1][x1];
        board[y1][x1] = 00;
        board[y2][x2] = piece;

        boolean attackPerformed = performAttack(y2, x2);
        System.out.printf("Move: %s %s\n", getAlgebraicNotation(y1, x1, y2, x2), attackPerformed? "Hi-YA!" : "");
    }

    private boolean performAttack(int y, int x) {
        int rangeAttacker = (board[y][x] / 10) * 10; // Integer division sets the ones place to 0
        int forwardY = (rangeAttacker == CPUAI_RANGE) ? 1 : -1;

        if (!inBounds(y+forwardY, x)) return false; // Attack would land off the board

        int rangeDefender = (board[y+forwardY][x] / 10) * 10;
        if (rangeDefender == 0 || rangeDefender == rangeAttacker) return false; // Not a valid target

        // If we're this far, we have a valid target that we are going to attack.
        switch (board[y+forwardY][x] % 10) {
            case 1: // Mini Ninja
            case 5: // Mini Samurai
                board[y+forwardY][x] = 00; // The mini pieces are killed
                break;
            case 2: // Norm Ninja
            case 6: // Norm Samurai
                board[y+forwardY][x]--; // Demote the piece
                break;
            case 9: // The King!
                board[y+forwardY][x] = 00; // The King is killed - game over
                isGameOver = true;
                break;
            default:
                throw new IllegalStateException();
        }

        return true;
    }

    private String getPlayerMove(List<String> validMoves) {
        String move;

        while (true) {
            System.out.printf("Valid moves are: %s\n", String.join(", ", validMoves));
            System.out.print("Please enter your desired move: ");
            move = in.next().toUpperCase();

            if (validMoves.contains(move)) {
                return move;
            } else {
                System.out.println("That is not a valid move.\n");
            }
        }
    }

    private List<String> getValidMoves(int range) {
        List<String> validMoves = new LinkedList<>();
        int forwardY = (range == CPUAI_RANGE) ? 1 : -1; // The computer considers down (+y) as "forward"

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                int pieceType = board[y][x] - range;
                if (pieceType >= 0 && pieceType <= 9) {
                    addValidMoves(validMoves, y, x, forwardY, range);
                }
            }
        }

        return validMoves;
    }

    private void addValidMoves(List<String> validMoves, int y, int x, int forwardY, int range) {
        boolean isMini = false;
        int testY;
        int testX;

        switch (board[y][x] % 10) {
            case 1: // Mini Ninja
                isMini = true;
            case 2: // Norm Ninja
                testY = y;
                testX = x;
                do {
                    // Forward left
                    testY += forwardY;
                    testX -= 1;

                    if(inBounds(testY, testX) && board[testY][testX] == 00) {
                        validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                    } else break;
                } while (!isMini);

                testY = y;
                testX = x;
                do {
                    // Forward right
                    testY += forwardY;
                    testX += 1;

                    if(inBounds(testY, testX) && board[testY][testX] == 00) {
                        validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                    } else break;
                } while (!isMini);

                testY = y;
                testX = x;
                do {
                    // Back left
                    testY -= forwardY;
                    testX -= 1;

                    if(inBounds(testY, testX) && inBounds(testY+forwardY, testX) && board[testY][testX] == 00) {
                        int victimPiece = board[testY+forwardY][testX];
                        if (victimPiece != 00 && !(victimPiece > range && victimPiece < range+10)) {
                            // There is an enemy piece that we can attack
                            validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                        }
                    } else break;
                } while (!isMini);

                testY = y;
                testX = x;
                do {
                    // Back right
                    testY -= forwardY;
                    testX += 1;

                    if(inBounds(testY, testX) && inBounds(testY+forwardY, testX) && board[testY][testX] == 00) {
                        int victimPiece = board[testY+forwardY][testX];
                        if (victimPiece != 00 && !(victimPiece > range && victimPiece < range+10)) {
                            // There is an enemy piece that we can attack
                            validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                        }
                    } else break;
                } while (!isMini);

                return;
            case 5: // Mini Samurai
                isMini = true;
            case 6: // Norm Samurai
                testY = y;
                testX = x;
                do {
                    // Forward
                    testY += forwardY;

                    if(inBounds(testY, testX) && board[testY][testX] == 00) {
                        validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                    } else break;
                } while (!isMini);

                testY = y;
                testX = x;
                do {
                    // Left
                    testX -= 1;

                    if(inBounds(testY, testX) && inBounds(testY+forwardY, testX) && board[testY][testX] == 00) {
                        int victimPiece = board[testY+forwardY][testX];
                        if (victimPiece != 00 && !(victimPiece > range && victimPiece < range+10)) {
                            // There is an enemy piece that we can attack
                            validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                        }
                    } else break;
                } while (!isMini);

                testY = y;
                testX = x;
                do {
                    // Right
                    testX += 1;

                    if(inBounds(testY, testX) && inBounds(testY+forwardY, testX) && board[testY][testX] == 00) {
                        int victimPiece = board[testY+forwardY][testX];
                        if (victimPiece != 00 && !(victimPiece > range && victimPiece < range+10)) {
                            // There is an enemy piece that we can attack
                            validMoves.add(getAlgebraicNotation(y, x, testY, testX));
                        }
                    } else break;
                } while (!isMini);

                return;
            case 9: // The King
                // The King has no possible moves.
        }
    }

    private String getAlgebraicNotation(int fromY, int fromX, int toY, int toX) {
        return new String(new char[] {
                (char)((int)'A' + fromX),
                (char)((int)'0' + (8 - fromY)),
                (char)((int)'A' + toX),
                (char)((int)'0' + (8 - toY)),
        });
    }

    private boolean inBounds(int testY, int testX) {
        return testX >= 0 && testX <= 6 && testY >= 0 && testY <= 7;
    }

    private void init() {
    /* Pieces:
     * 00 - empty
     * Computer:
     *  11 - Mini Ninja (Bishop)
     *  12 - Norm Ninja
     *  15 - Mini Samurai (Rook)
     *  16 - Norm Samurai
     *  19 - King
     *
     * Human:
     *  21 - Mini Ninja (Bishop)
     *  22 - Norm Ninja
     *  25 - Mini Samurai (Rook)
     *  26 - Norm Samurai
     *  29 - King
     */
        board = new int[][] {
                {00, 00, 00, 19, 00, 00, 00},
                {12, 12, 12, 00, 16, 16, 16},
                {15, 15, 15, 00, 11, 11, 11},
                {00, 00, 00, 00, 00, 00, 00},
                {00, 00, 00, 00, 00, 00, 00},
                {25, 25, 25, 00, 21, 21, 21},
                {22, 22, 22, 00, 26, 26, 26},
                {00, 00, 00, 29, 00, 00, 00},
        };

        isGameOver = false;
        in = new Scanner(System.in);
    }


    private void printBoard() {
        ColoredPrinter coloredPrinter = new ColoredPrinter.Builder(1, false).build();
        boolean blueBG = true;

        coloredPrinter.println("");
        coloredPrinter.println("   --------------------- COMPUTER");
        for(int y = 0; y < 8; y++) {
            coloredPrinter.print(String.format(" %d ", 8 - y));
            for (int x = 0; x < 7; x++) {
                int piece = board[y][x];
                Ansi.BColor bColor = blueBG ? Ansi.BColor.BLUE : Ansi.BColor.BLACK;
                Ansi.FColor fColor = piece >= 20 ? Ansi.FColor.WHITE : Ansi.FColor.GREEN;
                Ansi.Attribute attribute = Ansi.Attribute.LIGHT;
                String text = pieceToString(piece);

                coloredPrinter.print(text, attribute, fColor, bColor);

                blueBG = !blueBG;
            }
            coloredPrinter.clear();
            coloredPrinter.println("");
        }
        coloredPrinter.println("   --------------------- HUMAN");
        coloredPrinter.println("    A  B  C  D  E  F  G ");
    }

    private String pieceToString(int piece) {
        switch (piece % 10) {
            case 0:
                return "   ";
            case 1:
                return " j ";
            case 2:
                return " J ";
            case 5:
                return " s ";
            case 6:
                return " S ";
            case 9:
                return " K ";
            default:
                throw new IllegalArgumentException();
        }
    }

}
