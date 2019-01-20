package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = { -33, -31, -18, -14, 14, 18, 31, 33 };

    public Knight(Alliance alliance, int position) {
        super(alliance, position, PieceType.KNIGHT, true);
    }

    public Knight(Alliance alliance, int position, boolean isFirstMove) {
        super(alliance, position, PieceType.KNIGHT, isFirstMove);
    }

    @Override
    public Knight movePiece(Move move) {
        return PieceUtils.INSTANCE.getMovedKnight(move.getMovedPiece().getAlliance(), move.getDestinationCoordinate());
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.position + currentCandidateOffset;
            if (isValidTileCoordinate(candidateDestinationCoordinate)) {
                final Piece pieceAtDestination = board.getPiece(candidateDestinationCoordinate);
                if (pieceAtDestination == null) {
                    legalMoves.add(new Move.MajorMove(board, candidateDestinationCoordinate, this));
                } else {
                    final Alliance pieceAtDestinationAllegiance = pieceAtDestination.getAlliance();
                    if (this.alliance != pieceAtDestinationAllegiance) {
                        legalMoves.add(new Move.AttackMove(board, candidateDestinationCoordinate, this,
                                pieceAtDestination));
                    }
                }
            }
        }
        return legalMoves;
    }
}
