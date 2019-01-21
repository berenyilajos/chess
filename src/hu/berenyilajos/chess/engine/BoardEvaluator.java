package hu.berenyilajos.chess.engine;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.pieces.Piece;
import hu.berenyilajos.chess.pieces.PieceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (board == b) return false;
        if (board.currentPlayer().getAlliance() != b.currentPlayer().getAlliance() ||
                board.currentPlayer().getActivePieces().size() != b.currentPlayer().getActivePieces().size() ||
                board.currentPlayer().getOpponent().getActivePieces().size() != b.currentPlayer().getOpponent().getActivePieces().size() ||
                board.currentPlayer().isCastled() && !b.currentPlayer().isCastled() ||
                board.currentPlayer().isCastled() && !b.currentPlayer().isCastled() ||
                board.currentPlayer().getOpponent().isCastled() && !b.currentPlayer().getOpponent().isCastled() ||
                board.currentPlayer().getOpponent().isCastled() && !b.currentPlayer().getOpponent().isCastled() ||
                board.getBoardConfig().size() != b.getBoardConfig().size()
        ) {
            return false;
        }

        for (Map.Entry<Integer, Piece> e : b.getBoardConfig().entrySet()) {
            if (!e.getValue().equals(board.getBoardConfig().get(e.getKey()))) {
                return false;
            }
        }

        return true;
    }

    static boolean piecesEquals(Board b, Board board) {
        return piecesEquals(b.getWhitePieces(), board.getWhitePieces()) &&
                piecesEquals(b.getBlackPieces(), board.getBlackPieces());
    }

    static boolean piecesEquals(List<Piece> p1, List<Piece> p2) {
        Map<PieceType, Integer> m = new HashMap<>(6);
        for (Piece p : p1) {
            if (m.get(p.getPieceType()) == null) {
                m.put(p.getPieceType(), 1);
            } else {
                m.put(p.getPieceType(), m.get(p.getPieceType()) + 1);
            }
        }
        for (Piece p : p2) {
            if ((m.get(p.getPieceType()) == null) || (m.get(p.getPieceType()) == 0)) {
                return false;
            }
            m.put(p.getPieceType(), m.get(p.getPieceType()) - 1);
        }
        for (Integer i : m.values()) {
            if (i != 0) {
                return false;
            }
        }

        return true;
    }

}
