package hu.berenyilajos.chess.pieces;

import hu.berenyilajos.chess.player.BlackPlayer;
import hu.berenyilajos.chess.player.Player;
import hu.berenyilajos.chess.player.WhitePlayer;

public enum  Alliance {
    WHITE {
        @Override
        public Player choosePlayerByAlliance(WhitePlayer whitePlayer, BlackPlayer blackPlayer) {
            return whitePlayer;
        }

        @Override
        public int getOppositeDirection() {
            return UP_DIRECTION;
        }

        @Override
        public int getPositionByDirection(int i) {
            return -i;
        }

        @Override
        public boolean isPawnPromotionSquare(int candidateDestinationCoordinate) {
            return (candidateDestinationCoordinate & 0xF8) == 0;
        }

        @Override
        public boolean isSecondRow(int position) {
            return (position & 0xF8) == 0x60;
        }
    },
    BLACK {
        @Override
        public Player choosePlayerByAlliance(WhitePlayer whitePlayer, BlackPlayer blackPlayer) {
            return blackPlayer;
        }

        @Override
        public int getOppositeDirection() {
            return DOWN_DIRECTION;
        }

        @Override
        public int getPositionByDirection(int i) {
            return i;
        }

        @Override
        public boolean isPawnPromotionSquare(int candidateDestinationCoordinate) {
            return (candidateDestinationCoordinate & 0xF8) == 0x70;
        }

        @Override
        public boolean isSecondRow(int position) {
            return (position & 0xF8) == 0x10;
        }
    };

    private static int UP_DIRECTION = -1;
    private static int DOWN_DIRECTION = 1;

    public abstract Player choosePlayerByAlliance(WhitePlayer whitePlayer, BlackPlayer blackPlayer);

    public abstract int getOppositeDirection();

    public abstract int getPositionByDirection(int i);

    public abstract boolean isPawnPromotionSquare(int candidateDestinationCoordinate);

    public abstract boolean isSecondRow(int position);
}
