package hu.berenyilajos.chess.player;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.BoardUtils;
import hu.berenyilajos.chess.board.Move;
import hu.berenyilajos.chess.pieces.Alliance;
import hu.berenyilajos.chess.pieces.Piece;
import hu.berenyilajos.chess.pieces.PieceType;
import hu.berenyilajos.chess.pieces.Rook;

import java.util.ArrayList;
import java.util.List;

public class BlackPlayer extends Player {

    public BlackPlayer(Board board, List<Move> playerLegals, List<Move> opponentLegals) {
        super(board, playerLegals, opponentLegals);
    }

    @Override
    public List<Move> calculateKingCastles(List<Move> playerLegals, List<Move> opponentLegals) {
        if(this.isInCheck() || this.isCastled() || !(this.isKingSideCastleCapable() || this.isQueenSideCastleCapable())) {
            return new ArrayList<>(0);
        }

        final List<Move> kingCastles = new ArrayList<>(2);

        if(this.playerKing.isFirstMove() && this.playerKing.getPosition() == 0x04 && !this.isInCheck()) {
            //whites king side castle
            if(this.board.getPiece(0x05) == null && this.board.getPiece(0x06) == null) {
                final Piece kingSideRook = this.board.getPiece(0x07);
                if(kingSideRook != null && kingSideRook.isFirstMove()) {
                    if(!Player.isAttackOnTile(0x05, opponentLegals) &&
                            !Player.isAttackOnTile(0x06, opponentLegals) && kingSideRook.is(PieceType.ROOK)) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 0x14)) {
                            kingCastles.add(new Move.KingSideCastleMove(this.board, this.playerKing, 0x06, (Rook) kingSideRook, kingSideRook.getPosition(), 0x05));
                        }
                    }
                }
            }
            //whites queen side castle
            if(this.board.getPiece(0x03) == null && this.board.getPiece(0x02) == null &&
                    this.board.getPiece(0x00) == null) {
                final Piece queenSideRook = this.board.getPiece(0x00);
                if(queenSideRook != null && queenSideRook.isFirstMove()) {
                    if(!Player.isAttackOnTile(0x02, opponentLegals) &&
                            !Player.isAttackOnTile(0x03, opponentLegals) && queenSideRook.is(PieceType.ROOK)) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 0x14)) {
                            kingCastles.add(new Move.QueenSideCastleMove(this.board, this.playerKing, 0x02, (Rook) queenSideRook, queenSideRook.getPosition(), 0x03));
                        }
                    }
                }
            }
        }
        return kingCastles;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    public List<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public boolean isWhite() {
        return false;
    }

    @Override
    public boolean isBlack() {
        return true;
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }
}
