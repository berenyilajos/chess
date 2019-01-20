package hu.berenyilajos.chess.board;

import com.google.common.collect.Iterables;
import hu.berenyilajos.chess.pieces.*;
import hu.berenyilajos.chess.player.BlackPlayer;
import hu.berenyilajos.chess.player.Player;
import hu.berenyilajos.chess.player.WhitePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class Board {

    private final Map<Integer, Piece> boardConfig;
    private final List<Piece> whitePieces;
    private final List<Piece> blackPieces;
    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;
    private final Pawn enPassantPawn;
    private final Move transitionMove;

    private static final Board STANDARD_BOARD = createStandardBoardImpl();

    private List<Move> allLegalMoves;

    private Board(Builder builder) {
        this.boardConfig = builder.boardConfig;
        this.whitePieces = calculateActivePieces(Alliance.WHITE);
        this.blackPieces = calculateActivePieces(Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;
        final List<Move> whiteStandardMoves = calculateLegalMoves(this.whitePieces);
        final List<Move> blackStandardMoves = calculateLegalMoves(this.blackPieces);
        this.whitePlayer = new WhitePlayer(this, whiteStandardMoves, blackStandardMoves);
        this.blackPlayer = new BlackPlayer(this, blackStandardMoves, whiteStandardMoves);
        this.currentPlayer = builder.nextMoveMaker.choosePlayerByAlliance(this.whitePlayer, this.blackPlayer);
        this.transitionMove = builder.transitionMove != null ? builder.transitionMove : Move.MoveFactory.getNullMove();
    }

    private List<Move> calculateLegalMoves(List<Piece> pieces) {
        return pieces.stream().flatMap(piece -> piece.calculateLegalMoves(this).stream()).collect(Collectors.toList());
    }

    private List<Piece> calculateActivePieces(Alliance alliance) {
        return boardConfig.values().stream()
               .filter(piece -> piece.getAlliance() == alliance)
               .collect(Collectors.toList());
    }

    public Piece getPiece(int position) {
        return boardConfig.get(position);
    }

    public Pawn getEnPassantPawn() {
        return enPassantPawn;
    }

    public Move getTransitionMove() {
        return transitionMove;
    }

    public static Board createStandardBoard() {
        return STANDARD_BOARD;
    }

    public static class Builder {

        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;
        Move transitionMove;

        public Builder() {
            this.boardConfig = new HashMap<>(33, 1.0f);
        }

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }

        public Builder setEnPassantPawn(final Pawn enPassantPawn) {
            this.enPassantPawn = enPassantPawn;
            return this;
        }

        public Builder setMoveTransition(final Move transitionMove) {
            this.transitionMove = transitionMove;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
    }

    public Player currentPlayer() {
        return currentPlayer;
    }

    public List<Move> getAllLegalMoves() {
        if (allLegalMoves != null) {
            return  allLegalMoves;
        }
        allLegalMoves = new ArrayList<>(this.whitePlayer.getLegalMoves());
        allLegalMoves.addAll(this.blackPlayer.getLegalMoves());
        return allLegalMoves;
    }

    public Player blackPlayer() {
        return blackPlayer;
    }

    public Player whitePlayer() {
        return whitePlayer;
    }

    private static Board createStandardBoardImpl() {
        final Builder builder = new Builder();
        // Black Layout
        builder.setPiece(new Rook(Alliance.BLACK, 0));
        builder.setPiece(new Knight(Alliance.BLACK, 1));
        builder.setPiece(new Bishop(Alliance.BLACK, 2));
        builder.setPiece(new Queen(Alliance.BLACK, 3));
        builder.setPiece(new King(Alliance.BLACK, 4, true, true));
        builder.setPiece(new Bishop(Alliance.BLACK, 5));
        builder.setPiece(new Knight(Alliance.BLACK, 6));
        builder.setPiece(new Rook(Alliance.BLACK, 7));
        builder.setPiece(new Pawn(Alliance.BLACK, 16));
        builder.setPiece(new Pawn(Alliance.BLACK, 17));
        builder.setPiece(new Pawn(Alliance.BLACK, 18));
        builder.setPiece(new Pawn(Alliance.BLACK, 19));
        builder.setPiece(new Pawn(Alliance.BLACK, 20));
        builder.setPiece(new Pawn(Alliance.BLACK, 21));
        builder.setPiece(new Pawn(Alliance.BLACK, 22));
        builder.setPiece(new Pawn(Alliance.BLACK, 23));
        // White Layout
        builder.setPiece(new Pawn(Alliance.WHITE, 96));
        builder.setPiece(new Pawn(Alliance.WHITE, 97));
        builder.setPiece(new Pawn(Alliance.WHITE, 98));
        builder.setPiece(new Pawn(Alliance.WHITE, 99));
        builder.setPiece(new Pawn(Alliance.WHITE, 100));
        builder.setPiece(new Pawn(Alliance.WHITE, 101));
        builder.setPiece(new Pawn(Alliance.WHITE, 102));
        builder.setPiece(new Pawn(Alliance.WHITE, 103));
        builder.setPiece(new Rook(Alliance.WHITE, 112));
        builder.setPiece(new Knight(Alliance.WHITE, 113));
        builder.setPiece(new Bishop(Alliance.WHITE, 114));
        builder.setPiece(new Queen(Alliance.WHITE, 115));
        builder.setPiece(new King(Alliance.WHITE, 116, true, true));
        builder.setPiece(new Bishop(Alliance.WHITE, 117));
        builder.setPiece(new Knight(Alliance.WHITE, 118));
        builder.setPiece(new Rook(Alliance.WHITE, 119));
        //white to move
        builder.setMoveMaker(Alliance.WHITE);
        //build the board
        return builder.build();
    }

    public static Board getStandardBoard() {
        return STANDARD_BOARD;
    }

    public List<Piece> getWhitePieces() {
        return whitePieces;
    }

    public List<Piece> getBlackPieces() {
        return blackPieces;
    }

    public Iterable<Piece> getAllPieces() {
        return Iterables.concat(this.whitePieces, this.blackPieces);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int position = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = boardConfig.get(position);
                builder.append(piece != null ? " " + piece + " " : " - ");
                position++;
            }
            builder.append("\n");
            position += 8;
        }

        return builder.toString();
    }
}
