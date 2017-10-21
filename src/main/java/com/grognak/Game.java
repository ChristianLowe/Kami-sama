package com.grognak;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    private static final int CPUAI_RANGE = 10;
    private static final int HUMAN_RANGE = 20;
    private static final int MAX_DEPTH = 50_000;

    private Map<Integer, List<String>> zobristMapCPUAI;
    private Map<Integer, List<String>> zobristMapHUMAN;
    private long[][][] zobrist;
    private int[][] board;
    private int[] history;
    private boolean isGameOver;
    private Scanner in;

    private int totalPrunes;

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
                System.out.println("Press Ctrl+C to quit...");
                try {
                    while (true) in.nextLine(); // :)
                } catch (NoSuchElementException e) { return; }
            }

            if (playerTurn) {
                String move = getPlayerMove(validMoves);
                System.out.println(performMove(move));
            } else {
                String move = getComputerMove(validMoves);

                System.out.println(performMove(move));
                System.out.println("My options were: "+String.join(", ", validMoves));
            }

            playerTurn = !playerTurn;
        }
    }

    private String getComputerMove(List<String> validMoves) {
        AtomicReference<String> move = new AtomicReference<>();
        TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newFixedThreadPool(1));

        int[][] backupBoard = Arrays.stream(board)
                .map(int[]::clone)
                .toArray(int[][]::new);
        try {
            System.out.println("*** THINKING ***");
            totalPrunes = 0;
            timeLimiter.runWithTimeout(() -> {
                for (int depth = 1; depth <= MAX_DEPTH; depth++) {
                    System.out.println("DEPTH: " + depth);
                    String newMove = minimax(validMoves, depth);
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    } else {
                        System.out.println("New move: " + newMove);
                        move.set(newMove);
                    }
                    if (isGameOver) { break; }
                }
                System.out.println("*** ;) ***");
            }, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("*** INTERRUPTED ***");
        }
        System.out.println("Total prunes: " + totalPrunes);
        board = backupBoard;

        System.out.println("Final move: " + move.get());
        return move.get();
    }

    private String minimax(List<String> validMoves, int depth) {
        int bestScore = Integer.MIN_VALUE;
        String bestMove = null;

        depth--;

        int a = Integer.MIN_VALUE;
        int b = Integer.MAX_VALUE;
        for (String validMove : validMoves) {
            int[][] backupBoard = Arrays.stream(board)
                    .map(int[]::clone)
                    .toArray(int[][]::new);

            performMove(validMove);

            if (isGameOver) {
                bestMove = validMove;
                board = backupBoard;
                break;
            }

            int score = min(depth, a, b);
            if (score > bestScore) {
                bestScore = score;
                bestMove = validMove;
            }

            a = Math.max(a, bestScore);
            board = backupBoard;
            isGameOver = false;
            board = backupBoard;
        }

        return bestMove;
    }

    private Integer min(int depth, int a, int b)  {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }

        int bestScore = Integer.MAX_VALUE;

        if (depth == 0) {
            return evaluate(depth);
        } else {
            depth--;
        }

        List<String> validMoves = getValidMoves(HUMAN_RANGE);
        for (String validMove : validMoves) {
            int[][] backupBoard = Arrays.stream(board)
                    .map(int[]::clone)
                    .toArray(int[][]::new);

            performMove(validMove);

            if (isGameOver) {
                isGameOver = false;
                board = backupBoard;
                return Integer.MIN_VALUE;
            }

            int score = max(depth, a, b);
            bestScore = Math.min(bestScore, score);
            b = Math.min(b, bestScore);
            board = backupBoard;

            if (b <= a) {
                totalPrunes++;
                history[moveToHistoryIndex(validMove)] += depth;
                break;
            }
        }

        return bestScore;
    }

    private Integer max(int depth, int a, int b) {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }

        int bestScore = Integer.MIN_VALUE;

        if (depth == 0) {
            return evaluate(depth);
        } else {
            depth--;
        }

        List<String> validMoves = getValidMoves(CPUAI_RANGE);
        for (String validMove : validMoves) {
            int[][] backupBoard = Arrays.stream(board)
                    .map(int[]::clone)
                    .toArray(int[][]::new);

            performMove(validMove);

            if (isGameOver) {
                isGameOver = false;
                board = backupBoard;
                return Integer.MAX_VALUE;
            }

            int score = min(depth, a, b);
            bestScore = Math.max(bestScore, score);
            a = Math.max(a, score);
            board = backupBoard;

            if (b <= a) {
                totalPrunes++;
                history[moveToHistoryIndex(validMove)] += depth;
                break;
            }
        }

        return bestScore;
    }

    private int evaluate(int depth) {
        return scorePieces(CPUAI_RANGE, depth) - scorePieces(HUMAN_RANGE, depth);
    }

    private int scorePieces(int range, int depth) {
        int score = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                int pieceType = board[y][x] - range;
                if (pieceType >= 0 && pieceType <= 9) {
                    if (range == CPUAI_RANGE) {
                        score += 8 - y;
                    } else {
                        score += y;
                    }

                    switch (pieceType) {
                        case 1: // Mini Ninja
                        case 5: // Mini Samurai
                            score += 10;
                            break;
                        case 2: // Norm Ninja
                        case 6: // Norm Samurai
                            score += 30;
                            break;
                        case 9: // The King
                            score += 10000 - depth;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }
            }
        }

        return score;
    }

    private String performMove(String move) {
        char[] chars = move.toCharArray(); // Algebraic notation
        int x1 = chars[0] - 'A';
        int y1 = 8 - (chars[1] - '0');
        int x2 = chars[2] - 'A';
        int y2 = 8 - (chars[3] - '0');

        return performMove(y1, x1, y2, x2);
    }

    private String performMove(int y1, int x1, int y2, int x2) {
        int piece = board[y1][x1];
        board[y1][x1] = 00;
        board[y2][x2] = piece;

        boolean attackPerformed = performAttack(y2, x2);
        return String.format("\nMove: %s (%s) %s", getAlgebraicNotation(y1, x1, y2, x2), getInverseNotation(y1, x1, y2, x2), attackPerformed? "Hi-YA!" : "");
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
        // Check Zobrist table
        int hash = hash();
        List<String> zobristList;

        if (range == CPUAI_RANGE) {
            zobristList = zobristMapCPUAI.get(hash);
        } else {
            zobristList = zobristMapHUMAN.get(hash);
        }

        if (zobristList != null) {
            return zobristList;
        }

        // We have to calculate the moves
        List<String> validMoves = new ArrayList<>();
        int forwardY = (range == CPUAI_RANGE) ? 1 : -1; // The computer considers down (+y) as "forward"

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                int pieceType = board[y][x] - range;
                if (pieceType >= 0 && pieceType <= 9) {
                    addValidMoves(validMoves, y, x, forwardY, range);
                }
            }
        }

        // History table
        validMoves.sort(Comparator.comparingInt(s -> history[moveToHistoryIndex((String)s)]).reversed());

        // Add valid moves to Zobrist map
        if (range == CPUAI_RANGE) {
            zobristMapCPUAI.put(hash, validMoves);
        } else {
            zobristMapHUMAN.put(hash, validMoves);
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
                (char)((int)'8' - fromY),
                (char)((int)'A' + toX),
                (char)((int)'8' - toY),
        });
    }

    private String getInverseNotation(int fromY, int fromX, int toY, int toX) {
        return new String(new char[] {
                (char)((int)'G' - fromX),
                (char)((int)'1' + fromY),
                (char)((int)'G' - toX),
                (char)((int)'1' + toY),
        });
    }

    private int moveToHistoryIndex(String move) {
        char[] chars = move.toCharArray(); // Algebraic notation
        int x1 = chars[0] - 'A';
        int y1 = 8 - (chars[1] - '0');
        int x2 = chars[2] - 'A';
        int y2 = 8 - (chars[3] - '0');

        return moveToHistoryIndex(y1, x1, y2, x2);
    }

    private int moveToHistoryIndex(int fromY, int fromX, int toY, int toX) {
        return fromY * 1000
                + fromX * 100
                + toY * 10
                + toX;
    }

    static private int[] hashReference;
    static {
        hashReference = new int[30];
        hashReference[11] = 0;
        hashReference[12] = 1;
        hashReference[15] = 2;
        hashReference[16] = 3;
        hashReference[19] = 4;
        hashReference[21] = 5;
        hashReference[22] = 6;
        hashReference[25] = 7;
        hashReference[26] = 8;
        hashReference[29] = 9;
    }

    private int hash() {
        int hash = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                if (board[y][x] != 00) {
                    int piece = board[y][x];
                    hash ^= zobrist[y][x][hashReference[piece]];
                }
            }
        }

        return hash;
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
                {21, 21, 21, 00, 25, 25, 25},
                {26, 26, 26, 00, 22, 22, 22},
                {00, 00, 00, 29, 00, 00, 00},
        };
        history = new int[8787];

        zobristMapCPUAI = new TreeMap<Integer, List<String>>();
        zobristMapHUMAN = new TreeMap<Integer, List<String>>();

        Random random = new Random();
        zobrist = new long[8][7][10];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                for (int p = 0; p < 10; p++) {
                    zobrist[y][x][p] = random.nextLong();
                }
            }
        }

        isGameOver = false;
        in = new Scanner(System.in);
    }


    private void printBoard() {
        ColoredPrinter coloredPrinter = new ColoredPrinter.Builder(1, false).build();
        boolean blueBG = true;

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
