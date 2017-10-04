package com.grognak;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

class Game {
    private int[][] board;
    Scanner in;

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

        System.out.println("Welcome! Here is a fresh, new board:");
        while (true) {
            printBoard();

            if (playerTurn) {
                String move = getPlayerMove();
                performMove(move);
            } else {
                System.out.println("The computer can't move yet...");
            }

            playerTurn = !playerTurn;

        }
    }

    private void performMove(String move) {
        char[] chars = move.toCharArray();
        int x1 = chars[0] - 'A';
        int y1 = 8 - (chars[1] - '0');
        int x2 = chars[2] - 'A';
        int y2 = 8 - (chars[3] - '0');

        int piece = board[y1][x1];
        board[y1][x1] = 00;
        board[y2][x2] = piece;

        performAttack(y2, x2);
    }

    private void performAttack(int y, int x) {
        return; // TODO
    }

    private String getPlayerMove() {
        String move;

        while (true) {
            System.out.printf("Valid moves are: %s\n", String.join(",", getValidMoves()));
            System.out.print("Please enter your desired move: ");
            move = in.next();

            if (isValidMove(move)) {
                return move;
            } else {
                System.out.println("That is not a valid move.\n");
            }
        }
    }

    private boolean isValidMove(String move) {
        return true;
        //TODO: After implementing getValidMoves():
        //return getValidMoves().contains(move);
    }

    private List<String> getValidMoves() {
        return new ArrayList<>(); // TODO
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
