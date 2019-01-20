package hu.berenyilajos.chess.board;

import hu.berenyilajos.chess.pieces.Pawn;
import hu.berenyilajos.chess.pieces.Piece;
import hu.berenyilajos.chess.pieces.Rook;

public abstract class Move {

    protected final Board board;
    protected final int destinationCoordinate;
    protected final Piece movedPiece;
    private final int hashCode;

    public Move(Board board, int destinationCoordinate, Piece movedPiece) {
        this.board = board;
        this.destinationCoordinate = destinationCoordinate;
        this.movedPiece = movedPiece;
        this.hashCode = calculateHashCode();
    }

    private int calculateHashCode() {
        int result = destinationCoordinate;
        result = 31 * result + (movedPiece != null ? movedPiece.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (destinationCoordinate != move.destinationCoordinate || movedPiece == null) return false;
        return movedPiece.equals(move.movedPiece);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public Board undo() {
        final Board.Builder builder = new Board.Builder();
        for (final Piece piece : this.board.getAllPieces()) {
            builder.setPiece(piece);
        }
        builder.setMoveMaker(this.board.currentPlayer().getAlliance());
        return builder.build();
    }

    public Board getBoard() {
        return this.board;
    }

    public int getCurrentCoordinate() {
        return this.movedPiece.getPosition();
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    public abstract boolean isAttack();

    public abstract boolean isCastlingMove();

    public abstract Piece getAttackedPiece();

    public abstract Board execute();

    protected String disambiguationFile() {
        for(final Move move : this.board.currentPlayer().getLegalMoves()) {
            if(move.getDestinationCoordinate() == this.destinationCoordinate && !this.equals(move) &&
                    this.movedPiece.getPieceType().equals(move.getMovedPiece().getPieceType())) {
                return BoardUtils.INSTANCE.getPositionAtCoordinate(this.movedPiece.getPosition()).substring(0, 1);
            }
        }
        return "";
    }

    protected Board baseExecute() {
        final Board.Builder builder = new Board.Builder();
        for (Piece piece : this.board.currentPlayer().getActivePieces()) {
            if (!this.movedPiece.equals(piece)) {
                builder.setPiece(piece);
            }
        }
        for (Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }
//        this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equalsForRepetition(piece)).forEach(builder::setPiece);
//        this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        builder.setMoveTransition(this);
        return builder.build();
    }

    public static class MajorMove extends Move {

        public MajorMove(Board board, int destinationCoordinate, Piece movedPiece) {
            super(board, destinationCoordinate, movedPiece);
        }

        @Override
        public boolean isAttack() {
            return false;
        }

        @Override
        public boolean isCastlingMove() {
            return false;
        }

        @Override
        public Piece getAttackedPiece() {
            return null;
        }

        @Override
        public Board execute() {
            return baseExecute();
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType().toString() + disambiguationFile() +
                    BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class AttackMove extends Move {

        private final Piece attackedPiece;

        public AttackMove(Board board, int destinationCoordinate, Piece movedPiece, Piece attackedPiece) {
            super(board, destinationCoordinate, movedPiece);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public boolean isCastlingMove() {
            return false;
        }

        @Override
        public Piece getAttackedPiece() {
            return attackedPiece;
        }

        @Override
        public Board execute() {
            return baseExecute();
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType() + disambiguationFile() + "x" +
                    BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnMove extends MajorMove {

        public PawnMove(Board board, int destinationCoordinate, Pawn movedPiece) {
            super(board, destinationCoordinate, movedPiece);
        }

        @Override
        public String toString() {
            return BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnPromotionMove extends PawnMove {

        final Move decoratedMove;
        final Pawn promotedPawn;
        final Piece promotionPiece;

        public PawnPromotionMove(final PawnMove decoratedMove,
                             final Piece promotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getDestinationCoordinate(), (Pawn) decoratedMove.getMovedPiece());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) getMovedPiece();
            this.promotionPiece = promotionPiece;
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Board.Builder();
            for (Piece piece : pawnMovedBoard.currentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (Piece piece : pawnMovedBoard.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
//            pawnMovedBoard.currentPlayer().getActivePieces().stream().filter(piece -> !this.promotedPawn.equalsForRepetition(piece)).forEach(builder::setPiece);
//            pawnMovedBoard.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
            builder.setPiece(this.promotionPiece.movePiece(this));
            builder.setMoveMaker(pawnMovedBoard.currentPlayer().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.INSTANCE.getPositionAtCoordinate(this.movedPiece.getPosition()) + "-" +
                    BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate) + "=" + this.promotionPiece.getPieceType();
        }
    }

    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(Board board, int destinationCoordinate, Pawn movedPiece, Piece attackedPiece) {
            super(board, destinationCoordinate, movedPiece, attackedPiece);
        }

        @Override
        public String toString() {
            return BoardUtils.INSTANCE.getPositionAtCoordinate(this.movedPiece.getPosition()).substring(0, 1) + "x" +
                    BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnPromotionAttackMove extends PawnAttackMove {

        final Move decoratedMove;
        final Pawn promotedPawn;
        final Piece promotionPiece;

        public PawnPromotionAttackMove(final PawnAttackMove decoratedMove, final Piece promotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getDestinationCoordinate(), (Pawn) decoratedMove.getMovedPiece(), decoratedMove.getAttackedPiece());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) getMovedPiece();
            this.promotionPiece = promotionPiece;
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Board.Builder();
            for (Piece piece : pawnMovedBoard.currentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (Piece piece : pawnMovedBoard.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
//            pawnMovedBoard.currentPlayer().getActivePieces().stream().filter(piece -> !this.promotedPawn.equalsForRepetition(piece)).forEach(builder::setPiece);
//            pawnMovedBoard.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
            builder.setPiece(this.promotionPiece.movePiece(this));
            builder.setMoveMaker(pawnMovedBoard.currentPlayer().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.INSTANCE.getPositionAtCoordinate(this.movedPiece.getPosition()) + "x" +
                    BoardUtils.INSTANCE.getPositionAtCoordinate(this.destinationCoordinate) + "=" + this.promotionPiece.getPieceType();
        }
    }

    public static class PawnEnPassantMove extends PawnAttackMove {

        public PawnEnPassantMove(Board board, int destinationCoordinate, Pawn movedPiece, Pawn attackedPiece) {
            super(board, destinationCoordinate, movedPiece, attackedPiece);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }
//            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equalsForRepetition(piece)).forEach(builder::setPiece);
//            this.board.currentPlayer().getOpponent().getActivePieces().stream().filter(piece -> !piece.equalsForRepetition(this.getAttackedPiece())).forEach(builder::setPiece);
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public Board undo() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.board.getAllPieces()) {
                builder.setPiece(piece);
            }
            builder.setEnPassantPawn((Pawn)this.getAttackedPiece());
            builder.setMoveMaker(this.board.currentPlayer().getAlliance());
            return builder.build();
        }
    }

    public static class PawnJumpMove extends PawnMove {

        public PawnJumpMove(Board board, int destinationCoordinate, Pawn movedPiece) {
            super(board, destinationCoordinate, movedPiece);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
//            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movedPiece.equalsForRepetition(piece)).forEach(builder::setPiece);
//            this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);
            final Pawn movedPawn = (Pawn)this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }
    }

    public static abstract class CastleMove extends Move {

        final Rook castleRook;
        final int castleRookStart;
        final int castleRookDestination;

        CastleMove(final Board board,
                   final Piece pieceMoved,
                   final int destinationCoordinate,
                   final Rook castleRook,
                   final int castleRookStart,
                   final int castleRookDestination) {
            super(board, destinationCoordinate, pieceMoved);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public boolean isAttack() {
            return false;
        }

        @Override
        public Piece getAttackedPiece() {
            return null;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.board.getAllPieces()) {
                if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            //calling movePiece here doesn't work, we need to explicitly create a new Rook
            builder.setPiece(new Rook(this.castleRook.getAlliance(), this.castleRookDestination, false));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }
    }

    public static class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(Board board, Piece pieceMoved, int destinationCoordinate,
                                  Rook castleRook, int castleRookStart, int castleRookDestination) {
            super(board, pieceMoved, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public String toString() {
            return "O-O";
        }
    }

    public static class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(Board board, Piece pieceMoved, int destinationCoordinate,
                                   Rook castleRook, int castleRookStart, int castleRookDestination) {
            super(board, pieceMoved, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public String toString() {
            return "O-O-0";
        }
    }

    private static class NullMove extends Move {

        private NullMove() {
            super(null, -1, null);
        }

        @Override
        public int getCurrentCoordinate() {
            return -1;
        }

        @Override
        public int getDestinationCoordinate() {
            return -1;
        }

        @Override
        public boolean isAttack() {
            return false;
        }

        @Override
        public boolean isCastlingMove() {
            return false;
        }

        @Override
        public Piece getAttackedPiece() {
            return null;
        }

        @Override
        public Board execute() {
            throw new RuntimeException("cannot execute null move!");
        }

        @Override
        public String toString() {
            return "Null Move";
        }
    }

    public static enum MoveStatus {

        DONE {
            @Override
            public boolean isDone() {
                return true;
            }
        },
        ILLEGAL_MOVE {
            @Override
            public boolean isDone() {
                return false;
            }
        },
        LEAVES_PLAYER_IN_CHECK {
            @Override
            public boolean isDone() {
                return false;
            }
        };

        public abstract boolean isDone();

    }

    public static class MoveFactory {

        private static final Move NULL_MOVE = new NullMove();

        private MoveFactory() {
            throw new RuntimeException("Not instantiatable!");
        }

        public static Move getNullMove() {
            return NULL_MOVE;
        }

        public static Move createMove(final Board board,
                                      final int currentCoordinate,
                                      final int destinationCoordinate) {
            for (final Move move : board.getAllLegalMoves()) {
                if (move.getCurrentCoordinate() == currentCoordinate &&
                        move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return NULL_MOVE;
        }
    }

}
