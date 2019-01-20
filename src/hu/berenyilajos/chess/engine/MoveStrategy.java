package hu.berenyilajos.chess.engine;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;

public interface MoveStrategy {

    long getNumBoardsEvaluated();

    Move execute(Board board);

}
