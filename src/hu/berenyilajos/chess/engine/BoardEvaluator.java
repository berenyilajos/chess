package hu.berenyilajos.chess.engine;

import hu.berenyilajos.chess.board.Board;

import java.util.List;

public interface BoardEvaluator {

    int evaluate(Board board, int depth);

    static boolean isRepeatedBoard(Board board, List<Board> whiteAktualRepeatedBoards, List<Board> blackAktualRepeatedBoards) {
        List<Board> aktualRepeatedBoards = board.currentPlayer().isWhite() ?
                whiteAktualRepeatedBoards : blackAktualRepeatedBoards;
        for (Board b : aktualRepeatedBoards) {
            if (equalsForRepetition(b, board)) {
                return true;
            }
        }

        return false;
    }

    static boolean equalsForRepetition(Board b, Board board) {
        return false;
    }

    static boolean piecesEquals(Board b, Board board) {
        return false;
    }

}
