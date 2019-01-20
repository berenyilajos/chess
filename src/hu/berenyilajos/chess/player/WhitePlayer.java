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

public class WhitePlayer extends Player {

    public WhitePlayer(Board board, List<Move> playerLegals, List<Move> opponentLegals) {
        super(board, playerLegals, opponentLegals);
    }

    @Override
    public List<Move> calculateKingCastles(List<Move> playerLegals, List<Move> opponentLegals) {
        if(this.isInCheck() || this.isCastled() || !(this.isKingSideCastleCapable() || this.isQueenSideCastleCapable())) {
            return new ArrayList<>(0);
        }

        final List<Move> kingCastles = new ArrayList<>(2);

        if(this.playerKing.isFirstMove() && this.playerKing.getPosition() == 0x74 && !this.isInCheck()) {
            //whites king side castle
            if(this.board.getPiece(0x75) == null && this.board.getPiece(0x76) == null) {
                final Piece kingSideRook = this.board.getPiece(0x77);
                if(kingSideRook != null && kingSideRook.isFirstMove()) {
                    if(!Player.isAttackOnTile(0x75, opponentLegals) &&
                            !Player.isAttackOnTile(0x76, opponentLegals) && kingSideRook.is(PieceType.ROOK)) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 0x64)) {
                            kingCastles.add(new Move.KingSideCastleMove(this.board, this.playerKing, 0x76, (Rook) kingSideRook, kingSideRook.getPosition(), 0x75));
                        }
                    }
                }
            }
            //whites queen side castle
            if(this.board.getPiece(0x73) == null && this.board.getPiece(0x72) == null &&
                    this.board.getPiece(0x70) == null) {
                final Piece queenSideRook = this.board.getPiece(0x70);
                if(queenSideRook != null && queenSideRook.isFirstMove()) {
                    if(!Player.isAttackOnTile(0x72, opponentLegals) &&
                            !Player.isAttackOnTile(0x73, opponentLegals) && queenSideRook.is(PieceType.ROOK)) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 0x64)) {
                            kingCastles.add(new Move.QueenSideCastleMove(this.board, this.playerKing, 0x72, (Rook) queenSideRook, queenSideRook.getPosition(), 0x73));
                        }
                    }
                }
            }
        }
        return kingCastles;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    public List<Piece> getActivePieces() {
        return board.getWhitePieces();
    }

    @Override
    public boolean isWhite() {
        return true;
    }

    @Override
    public boolean isBlack() {
        return false;
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }


}
