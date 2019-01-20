package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class NoPiece extends Piece {

    public NoPiece(Alliance alliance, int position) {

        super(null, -1, PieceType.NONE, false);
    }

    @Override
    public Piece movePiece(Move move) {
        return null;
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        return new ArrayList<>(0);
    }
}
