package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

import java.util.List;

public abstract class Piece {

    protected final Alliance alliance;
    protected final int position;
    protected final PieceType pieceType;
    protected final boolean isFirstMove;
    protected final int hashCode;

    public Piece(Alliance alliance, int position, PieceType pieceType, boolean isFirstMove) {
        this.alliance = alliance;
        this.position = position;
        this.pieceType = pieceType;
        this.isFirstMove = isFirstMove;
        this.hashCode = calculateHashCode();
    }

    private int calculateHashCode() {
        int result = alliance.hashCode();
        result = 31 * result + position;
        result = 31 * result + pieceType.hashCode();
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public int getPosition() {
        return position;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public int getPieceValue() {
        return pieceType.value;
    }

    public abstract Piece movePiece(Move move);

    public abstract List<Move> calculateLegalMoves(final Board board);

    protected static boolean isValidTileCoordinate(final int coordinate) {
        return (coordinate & 0x88) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Piece piece = (Piece) o;

        if (position != piece.position) return false;
        if (isFirstMove != piece.isFirstMove) return false;
        if (alliance != piece.alliance) return false;
        return pieceType == piece.pieceType;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean is(PieceType pieceType) {
        return this.pieceType == pieceType;
    }

    @Override
    public String toString() {
        return alliance == Alliance.WHITE ? pieceType.pieceName : pieceType.pieceName.toLowerCase();
    }
}
