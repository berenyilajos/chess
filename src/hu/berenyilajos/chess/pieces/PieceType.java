package hu.berenyilajos.chess.pieces;

public enum PieceType {

    KING(0, "K"),
    QUEEN(900, "Q"),
    BISHOP(350, "B"),
    KNIGHT(320, "N"),
    ROOK(500, "R"),
    PAWN(100, "P"),
    NONE(0, "-");

    final int value;
    final String pieceName;


    PieceType(int value, String pieceName) {
        this.value = value;
        this.pieceName = pieceName;
    }

    @Override
    public String toString() {
        return pieceName;
    }
}
