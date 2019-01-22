package hu.berenyilajos.chess.engine;

import hu.berenyilajos.chess.board.BoardUtils;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;
import hu.berenyilajos.chess.board.MoveTransition;
import hu.berenyilajos.chess.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static hu.berenyilajos.chess.board.BoardUtils.mvvlva;
import static hu.berenyilajos.chess.board.Move.MoveFactory;

public class StockAlphaBeta extends Observable implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private long boardsEvaluated;
    private long executionTime;
    private int quiescenceCount;
    private final List<Board> whiteAktualRepeatedBoards;
    private final List<Board> blackAktualRepeatedBoards;
    private static final int MAX_QUIESCENCE = 5000*10;
    private int score;

    private enum MoveSorter {

        STANDARD {
            @Override
            List<Move> sort(final List<Move> moves) {
//                Collections.sort(moves, (Move move1, Move move2) -> {
//                    boolean isCastlingMove1 = move1.isCastlingMove();
//                    boolean isCastlingMove2 = move2.isCastlingMove();
//                    if (isCastlingMove1 && !isCastlingMove2) {
//                        return -1;
//                    } else if (!isCastlingMove1 && isCastlingMove2) {
//                        return 1;
//                    }
//                    return mvvlva(move2) - mvvlva(move1);
//                });
//                return moves;
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(mvvlva(move2), mvvlva(move1))
                        .result()).sortedCopy(moves);
            }
        },
        EXPENSIVE {
            @Override
            List<Move> sort(final List<Move> moves) {
//                Collections.sort(moves, (Move move1, Move move2) -> {
//                    boolean kingThreat1 = BoardUtils.kingThreat(move1);
//                    boolean kingThreat2 = BoardUtils.kingThreat(move2);
//                    if (kingThreat1 && !kingThreat2) {
//                        return -1;
//                    } else if (!kingThreat1 && kingThreat2) {
//                        return 1;
//                    }
//                    boolean isCastlingMove1 = move1.isCastlingMove();
//                    boolean isCastlingMove2 = move2.isCastlingMove();
//                    if (isCastlingMove1 && !isCastlingMove2) {
//                        return -1;
//                    } else if (!isCastlingMove1 && isCastlingMove2) {
//                        return 1;
//                    }
//                    return mvvlva(move2) - mvvlva(move1);
//                });
//                return moves;
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.kingThreat(move1), BoardUtils.kingThreat(move2))
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(mvvlva(move2), mvvlva(move1))
                        .result()).immutableSortedCopy(moves);
            }
        };

        abstract List<Move> sort(List<Move> moves);
    }


    public StockAlphaBeta(final int searchDepth, Observer debugPanel, List<Board> whiteAktualRepeatedBoards, List<Board> blackAktualRepeatedBoards) {
        this.evaluator = StandardBoardEvaluator.get();
        this.searchDepth = searchDepth;
        this.boardsEvaluated = 0;
        this.quiescenceCount = 0;
        this.whiteAktualRepeatedBoards = whiteAktualRepeatedBoards;
        this.blackAktualRepeatedBoards = blackAktualRepeatedBoards;
        addObserver(debugPanel);
    }

    @Override
    public String toString() {
        return "StockAlphaBeta";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        Move bestMove = MoveFactory.getNullMove();
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);
        int moveCounter = 1;
        int numMoves = board.currentPlayer().getLegalMoves().size();

        for (final Move move : MoveSorter.EXPENSIVE.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            this.quiescenceCount = 0;
            final String s;
            if (moveTransition.getMoveStatus().isDone()) {
                final long candidateMoveStartTime = System.nanoTime();
                currentValue = currentPlayer.isWhite() ?
                        min(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue) :
                        max(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
                if (currentPlayer.isWhite() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                    if(moveTransition.getToBoard().blackPlayer().isInCheckMate()) {
                        score = StandardBoardEvaluator.CHECK_MATE_BONUS;
                        break;
                    }
                }
                else if (currentPlayer.isBlack() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                    if(moveTransition.getToBoard().whitePlayer().isInCheckMate()) {
                        score = -StandardBoardEvaluator.CHECK_MATE_BONUS;
                        break;
                    }
                }

                final String quiescenceInfo = " " + score(currentPlayer, highestSeenValue, lowestSeenValue) + " q: " +this.quiescenceCount;
                s = "\t" + toString() + "(" +this.searchDepth+ "), m: (" +moveCounter+ "/" +numMoves+ ") " + move + ", best:  " + bestMove

                        + quiescenceInfo + ", t: " +calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
            } else {
                s = "\t" + toString() + ", m: (" +moveCounter+ "/" +numMoves+ ") " + move + " is illegal! best: " +bestMove;
            }
            System.out.println(s);
            setChanged();
            notifyObservers(s);
            moveCounter++;
        }

        this.executionTime = System.currentTimeMillis() - startTime;
        final String result = board.currentPlayer() + " SELECTS " +bestMove+ " [#boards evaluated = " +this.boardsEvaluated+
                " time taken = " +this.executionTime+ " ms, score = " + score;
        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, rate = %.1f, score: %d\n", board.currentPlayer(),
                bestMove, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)), score);
        setChanged();
        notifyObservers(result);
        return bestMove;
    }

    private String score(final Player currentPlayer,
                                final int highestSeenValue,
                                final int lowestSeenValue) {
        if(currentPlayer.isWhite()) {
            score = highestSeenValue > StandardBoardEvaluator.CHECK_MATE_BONUS - 1 ? highestSeenValue - searchDepth + 1 :
                    highestSeenValue < -StandardBoardEvaluator.CHECK_MATE_BONUS + 1 ? highestSeenValue + searchDepth - 1 :
                            highestSeenValue;
            return "[score: " +score + "]";
        } else if(currentPlayer.isBlack()) {
            score = lowestSeenValue < -StandardBoardEvaluator.CHECK_MATE_BONUS + 1 ? lowestSeenValue + searchDepth - 1 :
                    lowestSeenValue > StandardBoardEvaluator.CHECK_MATE_BONUS - 1 ? lowestSeenValue - searchDepth + 1 :
                            lowestSeenValue;
            return "[score: " +score+ "]";
        }
        throw new RuntimeException("bad bad boy!");
    }

    private int max(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        if (board.currentPlayer().isInCheckMate()) {
            this.boardsEvaluated++;
            return -StandardBoardEvaluator.CHECK_MATE_BONUS - depth;
        }
        if (board.currentPlayer().isInStaleMate() || isRepeatedBoard(board)) {
            this.boardsEvaluated++;
            return 0;
        }
        if (depth == 0) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentHighest = highest;
        for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentHighest = Math.max(currentHighest, min(moveTransition.getToBoard(),
                        calculateQuiescenceDepth(moveTransition, depth), currentHighest, lowest));
                if (currentHighest >= lowest) {
                    return lowest;
                }
            }
        }
        return currentHighest;
    }

    private int min(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        if (board.currentPlayer().isInCheckMate()) {
            this.boardsEvaluated++;
            return StandardBoardEvaluator.CHECK_MATE_BONUS + depth;
        }
        if (board.currentPlayer().isInStaleMate() || isRepeatedBoard(board)) {
            this.boardsEvaluated++;
            return 0;
        }
        if (depth == 0) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentLowest = lowest;
        for (final Move move : MoveSorter.STANDARD.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentLowest = Math.min(currentLowest, max(moveTransition.getToBoard(),
                        calculateQuiescenceDepth(moveTransition, depth), highest, currentLowest));
                if (currentLowest <= highest) {
                    return highest;
                }
            }
        }
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final MoveTransition moveTransition,
                                         final int depth) {
        if(depth == 1 && this.quiescenceCount < MAX_QUIESCENCE) {
            int activityMeasure = 0;
            if (moveTransition.getToBoard().currentPlayer().isInCheck()) {
                activityMeasure += 1;
            }
            for(final Move move: BoardUtils.lastNMoves(moveTransition.getToBoard(), 2)) {
                if(move.isAttack()) {
                    activityMeasure += 1;
                }
            }
            if(activityMeasure >= 2) {
                this.quiescenceCount++;
                return 1;
            }
        }
        return depth - 1;
    }

    private boolean isRepeatedBoard(Board board) {
        return BoardEvaluator.isRepeatedBoard(board, whiteAktualRepeatedBoards, blackAktualRepeatedBoards);
    }

    private static String calculateTimeTaken(final long start, final long end) {
        final long timeTaken = (end - start) / 1000000;
        return timeTaken + " ms";
    }

}