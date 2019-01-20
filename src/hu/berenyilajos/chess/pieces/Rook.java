package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = { -16, -1, 1, 16 };

    public Rook(Alliance alliance, int position) {
        super(alliance, position, PieceType.ROOK, true);
    }

    public Rook(Alliance alliance, int position, boolean isFirstMove) {
        super(alliance, position, PieceType.ROOK, isFirstMove);
    }

    @Override
    public Rook movePiece(Move move) {
        return PieceUtils.INSTANCE.getMovedRook(move.getMovedPiece().getAlliance(), move.getDestinationCoordinate());
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.position;
            while (isValidTileCoordinate(candidateDestinationCoordinate += currentCandidateOffset)) {
                final Piece pieceAtDestination = board.getPiece(candidateDestinationCoordinate);
                if (pieceAtDestination == null) {
                    legalMoves.add(new Move.MajorMove(board, candidateDestinationCoordinate, this));
                } else {
                    final Alliance pieceAtDestinationAllegiance = pieceAtDestination.getAlliance();
                    if (this.alliance != pieceAtDestinationAllegiance) {
                        legalMoves.add(new Move.AttackMove(board, candidateDestinationCoordinate, this,
                                pieceAtDestination));
                    }
                    break;
                }
            }
        }
        return legalMoves;
    }
}
