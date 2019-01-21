package hu.berenyilajos.chess;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.gui.Table;

public class Chess {

    public static void main(String[] args) {
        Board board = Board.getStandardBoard();
        System.out.println(board);
        Table.get().show();
    }

}
