package hu.berenyilajos.chess.pieces;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

enum PieceUtils {

    INSTANCE;

    private final Table<Alliance, Integer, Queen> ALL_POSSIBLE_QUEENS = PieceUtils.createAllPossibleMovedQueens();
    private final Table<Alliance, Integer, Rook> ALL_POSSIBLE_ROOKS = PieceUtils.createAllPossibleMovedRooks();
    private final Table<Alliance, Integer, Knight> ALL_POSSIBLE_KNIGHTS = PieceUtils.createAllPossibleMovedKnights();
    private final Table<Alliance, Integer, Bishop> ALL_POSSIBLE_BISHOPS = PieceUtils.createAllPossibleMovedBishops();
    private final Table<Alliance, Integer, Pawn> ALL_POSSIBLE_PAWNS = PieceUtils.createAllPossibleMovedPawns();

    Pawn getMovedPawn(final Alliance alliance,
                      final int destinationCoordinate) {
        return ALL_POSSIBLE_PAWNS.get(alliance, destinationCoordinate);
    }

    Knight getMovedKnight(final Alliance alliance,
                          final int destinationCoordinate) {
        return ALL_POSSIBLE_KNIGHTS.get(alliance, destinationCoordinate);
    }

    Bishop getMovedBishop(final Alliance alliance,
                          final int destinationCoordinate) {
        return ALL_POSSIBLE_BISHOPS.get(alliance, destinationCoordinate);
    }

    Rook getMovedRook(final Alliance alliance,
                      final int destinationCoordinate) {
        return ALL_POSSIBLE_ROOKS.get(alliance, destinationCoordinate);
    }

    Queen getMovedQueen(final Alliance alliance,
                        final int destinationCoordinate) {
        return ALL_POSSIBLE_QUEENS.get(alliance, destinationCoordinate);
    }

    private static Table<Alliance, Integer, Pawn> createAllPossibleMovedPawns() {
        final ImmutableTable.Builder<Alliance, Integer, Pawn> pieces = ImmutableTable.builder();
        for(final Alliance alliance : Alliance.values()) {
            int index = 0;
            for(int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    pieces.put(alliance, index, new Pawn(alliance, index, false));
                    index++;
                }
                index += 8;
            }
        }
        return pieces.build();
    }

    private static Table<Alliance, Integer, Knight> createAllPossibleMovedKnights() {
        final ImmutableTable.Builder<Alliance, Integer, Knight> pieces = ImmutableTable.builder();
        for(final Alliance alliance : Alliance.values()) {
            int index = 0;
            for(int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    pieces.put(alliance, index, new Knight(alliance, index, false));
                    index++;
                }
                index += 8;
            }
        }
        return pieces.build();
    }

    private static Table<Alliance, Integer, Bishop> createAllPossibleMovedBishops() {
        final ImmutableTable.Builder<Alliance, Integer, Bishop> pieces = ImmutableTable.builder();
        for(final Alliance alliance : Alliance.values()) {
            int index = 0;
            for(int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    pieces.put(alliance, index, new Bishop(alliance, index, false));
                    index++;
                }
                index += 8;
            }
        }
        return pieces.build();
    }

    private static Table<Alliance, Integer, Rook> createAllPossibleMovedRooks() {
        final ImmutableTable.Builder<Alliance, Integer, Rook> pieces = ImmutableTable.builder();
        for(final Alliance alliance : Alliance.values()) {
            int index = 0;
            for(int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    pieces.put(alliance, index, new Rook(alliance, index, false));
                    index++;
                }
                index += 8;
            }
        }
        return pieces.build();
    }

    private static Table<Alliance, Integer, Queen> createAllPossibleMovedQueens() {
        final ImmutableTable.Builder<Alliance, Integer, Queen> pieces = ImmutableTable.builder();
        for(final Alliance alliance : Alliance.values()) {
            int index = 0;
            for(int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    pieces.put(alliance, index, new Queen(alliance, index, false));
                    index++;
                }
                index += 8;
            }
        }
        return pieces.build();
    }

}
