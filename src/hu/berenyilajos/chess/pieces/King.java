package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = { -17, -16, -15, -1, 1, 15, 16, 17 };

    private final boolean isCastled;
    private final boolean kingSideCastleCapable;
    private final boolean queenSideCastleCapable;

    public King(Alliance alliance, int position, boolean kingSideCastleCapable, boolean queenSideCastleCapable) {
        super(alliance, position, PieceType.KING, true);
        this.isCastled = false;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
    }

    public King(Alliance alliance, int position, boolean isFirstMove, boolean isCastled, boolean kingSideCastleCapable, boolean queenSideCastleCapable) {
        super(alliance, position, PieceType.KING, isFirstMove);
        this.isCastled = isCastled;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
    }

    @Override
    public King movePiece(Move move) {
        return new King(this.alliance, move.getDestinationCoordinate(), false, move.isCastlingMove(), false, false);
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

    @Override
    public int locationBonus() {
        return this.alliance.kingBonus(position);
    }

    public boolean isCastled() {
        return isCastled;
    }

    public boolean isKingSideCastleCapable() {
        return kingSideCastleCapable;
    }

    public boolean isQueenSideCastleCapable() {
        return queenSideCastleCapable;
    }
}
