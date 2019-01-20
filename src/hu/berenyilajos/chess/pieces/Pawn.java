package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = {0x10, 0x20, 0x0F, 0x11};

    public Pawn(Alliance alliance, int position) {
        super(alliance, position, PieceType.PAWN, true);
    }

    public Pawn(Alliance alliance, int position, boolean isFirstMove) {
        super(alliance, position, PieceType.PAWN, isFirstMove);
    }

    @Override
    public Pawn movePiece(Move move) {
        return PieceUtils.INSTANCE.getMovedPawn(move.getMovedPiece().getAlliance(), move.getDestinationCoordinate());
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.position + (this.alliance.getPositionByDirection(currentCandidateOffset));
            if (!isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }
            if (currentCandidateOffset == 0x10 && board.getPiece(candidateDestinationCoordinate) == null) {
                if (this.alliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                    legalMoves.add(new Move.PawnPromotionMove(
                            new Move.PawnMove(board, candidateDestinationCoordinate, this), PieceUtils.INSTANCE.getMovedQueen(this.alliance, candidateDestinationCoordinate)));
                    legalMoves.add(new Move.PawnPromotionMove(
                            new Move.PawnMove(board, candidateDestinationCoordinate, this), PieceUtils.INSTANCE.getMovedRook(this.alliance, candidateDestinationCoordinate)));
                    legalMoves.add(new Move.PawnPromotionMove(
                            new Move.PawnMove(board, candidateDestinationCoordinate, this), PieceUtils.INSTANCE.getMovedBishop(this.alliance, candidateDestinationCoordinate)));
                    legalMoves.add(new Move.PawnPromotionMove(
                            new Move.PawnMove(board, candidateDestinationCoordinate, this), PieceUtils.INSTANCE.getMovedKnight(this.alliance, candidateDestinationCoordinate)));
                }
                else {
                    legalMoves.add(new Move.PawnMove(board, candidateDestinationCoordinate, this));
                }
            }
            else if (currentCandidateOffset == 0x20 && this.isFirstMove() && this.alliance.isSecondRow(this.position)) {
                final int behindCandidateDestinationCoordinate =
                        this.position + (this.alliance.getPositionByDirection(0x10));
                if (board.getPiece(candidateDestinationCoordinate) == null &&
                        board.getPiece(behindCandidateDestinationCoordinate) == null) {
                    legalMoves.add(new Move.PawnJumpMove(board, candidateDestinationCoordinate, this));
                }
            }
            else if (currentCandidateOffset == 0x0F) {
                if(board.getPiece(candidateDestinationCoordinate) != null) {
                    final Piece pieceOnCandidate = board.getPiece(candidateDestinationCoordinate);
                    if (this.alliance != pieceOnCandidate.getAlliance()) {
                        if (this.alliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this, pieceOnCandidate), PieceUtils.INSTANCE.getMovedQueen(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this, pieceOnCandidate), PieceUtils.INSTANCE.getMovedRook(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this, pieceOnCandidate), PieceUtils.INSTANCE.getMovedBishop(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this, pieceOnCandidate), PieceUtils.INSTANCE.getMovedKnight(this.alliance, candidateDestinationCoordinate)));
                        }
                        else {
                            legalMoves.add(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this, pieceOnCandidate));
                        }
                    }
                } else if (board.getEnPassantPawn() != null && board.getEnPassantPawn().getPosition() ==
                        (this.position + (this.alliance.getOppositeDirection()))) {
                    final Pawn pieceOnCandidate = board.getEnPassantPawn();
                    if (this.alliance != pieceOnCandidate.getAlliance()) {
                        legalMoves.add(
                                new Move.PawnEnPassantMove(board, candidateDestinationCoordinate, this, pieceOnCandidate));

                    }
                }
            }
            else if (currentCandidateOffset == 0x11) {
                if(board.getPiece(candidateDestinationCoordinate) != null) {
                    if (this.alliance != board.getPiece(candidateDestinationCoordinate).getAlliance()) {
                        if (this.alliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this,
                                            board.getPiece(candidateDestinationCoordinate)), PieceUtils.INSTANCE.getMovedQueen(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this,
                                            board.getPiece(candidateDestinationCoordinate)), PieceUtils.INSTANCE.getMovedRook(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this,
                                            board.getPiece(candidateDestinationCoordinate)), PieceUtils.INSTANCE.getMovedBishop(this.alliance, candidateDestinationCoordinate)));
                            legalMoves.add(new Move.PawnPromotionAttackMove(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this,
                                            board.getPiece(candidateDestinationCoordinate)), PieceUtils.INSTANCE.getMovedKnight(this.alliance, candidateDestinationCoordinate)));
                        }
                        else {
                            legalMoves.add(
                                    new Move.PawnAttackMove(board, candidateDestinationCoordinate, this,
                                            board.getPiece(candidateDestinationCoordinate)));
                        }
                    }
                } else if (board.getEnPassantPawn() != null && board.getEnPassantPawn().getPosition() ==
                        (this.position - (this.alliance.getOppositeDirection()))) {
                    final Pawn pieceOnCandidate = board.getEnPassantPawn();
                    if (this.alliance != pieceOnCandidate.getAlliance()) {
                        legalMoves.add(
                                new Move.PawnEnPassantMove(board, candidateDestinationCoordinate, this, pieceOnCandidate));

                    }
                }
            }
        }
        return legalMoves;
    }
}
