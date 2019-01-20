package hu.berenyilajos.chess.player;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;
import hu.berenyilajos.chess.board.MoveTransition;
import hu.berenyilajos.chess.pieces.Alliance;
import hu.berenyilajos.chess.pieces.King;
import hu.berenyilajos.chess.pieces.Piece;
import hu.berenyilajos.chess.pieces.PieceType;

import java.util.ArrayList;
import java.util.List;

import static hu.berenyilajos.chess.board.Move.MoveStatus;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final List<Move> legalMoves;
    protected final boolean isInCheck;
    protected Boolean hasEscapeMoves;

    Player(final Board board,
           final List<Move> playerLegals,
           final List<Move> opponentLegals) {
        this.board = board;
        this.playerKing = establishKing();
        this.isInCheck = Player.isAttackOnTile(this.playerKing.getPosition(), opponentLegals);
        playerLegals.addAll(calculateKingCastles(playerLegals, opponentLegals));
        this.legalMoves = playerLegals;
    }

    public abstract List<Move> calculateKingCastles(List<Move> playerLegals, List<Move> opponentLegals);

    public static boolean isAttackOnTile(int tile, List<Move> opponentLegals) {
        List<Move> attacsOnTile = new ArrayList<>();
        for (Move move : opponentLegals) {
            if (move.getDestinationCoordinate() == tile) {
                return true;
            }
        }
        return false;
    }

    private King establishKing() {
        return (King) getActivePieces().stream().filter(piece ->
                piece.is(PieceType.KING)).findAny().orElseThrow(RuntimeException::new);
    }

    public boolean hasEscapeMoves() {
        if (hasEscapeMoves != null) {
            return hasEscapeMoves;
        }
        for (Move move : this.legalMoves) {
            if (makeMove(move).getMoveStatus().isDone()) {
                return hasEscapeMoves = true;
            }
        }

        return hasEscapeMoves = false;
    }

    public MoveTransition makeMove(final Move move) {
        if (!this.legalMoves.contains(move)) {
            return new MoveTransition(this.board, this.board, move, MoveStatus.ILLEGAL_MOVE);
        }
        final Board transitionedBoard = move.execute();
        final boolean kingAttacked = Player.isAttackOnTile(
                transitionedBoard.currentPlayer().getOpponent().getPlayerKing().getPosition(),
                transitionedBoard.currentPlayer().getLegalMoves());
        if (kingAttacked) {
            return new MoveTransition(this.board, this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }
        return new MoveTransition(this.board, transitionedBoard, move, MoveStatus.DONE);
    }

    public abstract Player getOpponent();

    public List<Move> getLegalMoves() {
        return legalMoves;
    }

    public King getPlayerKing() {
        return playerKing;
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    public boolean isCastled() {
        return this.playerKing.isCastled();
    }

    public boolean isKingSideCastleCapable() {
        return this.playerKing.isKingSideCastleCapable();
    }

    public boolean isQueenSideCastleCapable() {
        return this.playerKing.isQueenSideCastleCapable();
    }

    public MoveTransition unMakeMove(final Move move) {
        return new MoveTransition(this.board, move.undo(), move, MoveStatus.DONE);
    }

    public abstract List<Piece> getActivePieces();

    public abstract boolean isWhite();

    public abstract boolean isBlack();

    public abstract Alliance getAlliance();

    @Override
    public String toString() {
        return getAlliance().toString();
    }
}
