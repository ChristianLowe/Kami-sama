package com.grognak;

public class Game {
    private int[][] board;

    public Game() {
        /* Pieces:
         * 00 - empty
         * Computer:
         *  10 - King
         *  11 - Mini Ninja (Bishop)
         *  12 - Norm Ninja
         *  17 - Mini Samurai (Rook)
         *  18 - Norm Samurai
         *
         * Human:
         *  20 - King
         *  21 - Mini Ninja (Bishop)
         *  22 - Norm Ninja
         *  27 - Mini Samurai (Rook)
         *  28 - Norm Samurai
         */
        board = new int[][]{
                {00, 00, 00, 10, 00, 00, 00},
                {12, 12, 12, 00, 18, 18, 18},
                {17, 17, 17, 00, 11, 11, 11},
                {00, 00, 00, 00, 00, 00, 00},
                {00, 00, 00, 00, 00, 00, 00},
                {27, 27, 27, 00, 21, 21, 21},
                {22, 22, 22, 00, 28, 28, 28},
                {00, 00, 00, 20, 00, 00, 00},
        };
    }


}
