package com.grognak;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

class Game {
    private int[][] board;

    Game() {
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

        printBoard();
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
                Ansi.Attribute attribute = Ansi.Attribute.BOLD;
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
