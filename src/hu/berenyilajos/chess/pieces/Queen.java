package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = { -17, -16, -15, -1, 1, 15, 16, 17 };

    public Queen(Alliance alliance, int position) {
        super(alliance, position, PieceType.QUEEN, true);
    }

    public Queen(Alliance alliance, int position, boolean isFirstMove) {
        super(alliance, position, PieceType.QUEEN, isFirstMove);
    }

    @Override
    public Queen movePiece(Move move) {
        return PieceUtils.INSTANCE.getMovedQueen(move.getMovedPiece().getAlliance(), move.getDestinationCoordinate());
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

    @Override
    public int locationBonus() {
        return this.alliance.queenBonus(position);
    }
}
