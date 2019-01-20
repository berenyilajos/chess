package hu.berenyilajos.chess.pgn;


import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;
import hu.berenyilajos.chess.player.Player;

public interface PGNPersistence {

    void persistGame(Game game);

    Move getNextBestMove(Board board, Player player, String gameText);

}
