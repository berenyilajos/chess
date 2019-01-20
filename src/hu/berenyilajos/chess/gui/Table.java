package hu.berenyilajos.chess.gui;

import hu.berenyilajos.chess.engine.BoardEvaluator;
import hu.berenyilajos.chess.pieces.Alliance;
import hu.berenyilajos.chess.board.*;
import hu.berenyilajos.chess.board.Move.MoveFactory;
import hu.berenyilajos.chess.pieces.Piece;
import hu.berenyilajos.chess.pieces.PieceType;
import hu.berenyilajos.chess.player.Player;
import hu.berenyilajos.chess.engine.MoveStrategy;
import hu.berenyilajos.chess.engine.StandardBoardEvaluator;
import hu.berenyilajos.chess.engine.StockAlphaBeta;
import hu.berenyilajos.chess.pgn.FenUtilities;
import hu.berenyilajos.chess.pgn.MySqlGamePersistence;
import com.google.common.collect.Lists;
import hu.berenyilajos.chess.board.Move;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static hu.berenyilajos.chess.pgn.PGNUtilities.persistPGNFile;
import static hu.berenyilajos.chess.pgn.PGNUtilities.writeGameToPGNFile;
import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;
import static javax.swing.SwingUtilities.*;

public final class Table extends Observable {

    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final DebugPanel debugPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private final List<Board> gameBoards = new ArrayList<>();
    private final List<Board> repeatedBoards = new ArrayList<>();
    private List<Board> whiteAktualRepeatedBoards = new ArrayList<>();
    private List<Board> blackAktualRepeatedBoards = new ArrayList<>();
    private Board chessBoard;
    private Move computerMove;
    private Piece sourceTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private String pieceIconPath;
    private boolean highlightLegalMoves;
    private boolean useBook;
    private boolean whiteUsebook = true;
    private boolean blackUsebook = true;
    private Color lightTileColor = Color.decode("#FFFACD");
    private Color darkTileColor = Color.decode("#593E1A");

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);

    private static final Table INSTANCE = new Table();

    private Table() {
        this.gameFrame = new JFrame("BlackWidow");
        final JMenuBar tableMenuBar = new JMenuBar();
        populateMenuBar(tableMenuBar);
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setLayout(new BorderLayout());
        this.chessBoard = Board.createStandardBoard();
        this.boardDirection = BoardDirection.NORMAL;
        this.highlightLegalMoves = false;
        this.useBook = false;
        this.pieceIconPath = "art/holywarriors/";
        this.gameHistoryPanel = new GameHistoryPanel();
        this.debugPanel = new DebugPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.add(debugPanel, BorderLayout.SOUTH);
        setDefaultLookAndFeelDecorated(true);
        this.gameFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        center(this.gameFrame);
        this.gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        this.gameFrame.setVisible(true);
    }

    private void exit() {
        int result;
        Object[] selections = {"Yes", "No"};
        result = JOptionPane.showOptionDialog(
                this.gameFrame.getRootPane(),
                "Would you like to really exit?",
                "Exit?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                selections,
                selections[1]
        );
        if (result == JOptionPane.YES_OPTION) {
            Table.get().getGameFrame().dispose();
            System.exit(0);
        }
    }

    public static Table get() {
        return INSTANCE;
    }

    private JFrame getGameFrame() {
        return this.gameFrame;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private DebugPanel getDebugPanel() {
        return this.debugPanel;
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private boolean getHighlightLegalMoves() {
        return this.highlightLegalMoves;
    }

    private boolean getUseBook() {
        return this.useBook &&
            (Table.get().getGameBoard().currentPlayer().isWhite() && whiteUsebook ||
                    Table.get().getGameBoard().currentPlayer().isBlack() && blackUsebook);
    }

    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
        Table.get().getDebugPanel().redo();
    }

    private void populateMenuBar(final JMenuBar tableMenuBar) {
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
    }

    private static void center(final JFrame frame) {
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = frame.getSize().width;
        final int h = frame.getSize().height;
        final int x = (dim.width - w) / 2;
        final int y = (dim.height - h) / 2;
        frame.setLocation(x, y);
    }

    private JMenu createFileMenu() {
        final JMenu filesMenu = new JMenu("File");
        filesMenu.setMnemonic(KeyEvent.VK_F);

        final JMenuItem openPGN = new JMenuItem("Load PGN File", KeyEvent.VK_O);
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int option = chooser.showOpenDialog(Table.get().getGameFrame());
                if (option == JFileChooser.APPROVE_OPTION) {
                    loadPGNFile(chooser.getSelectedFile());
                }
            }
        });
        filesMenu.add(openPGN);

        final JMenuItem openFEN = new JMenuItem("Load FEN File", KeyEvent.VK_F);
        openFEN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String fenString = JOptionPane.showInputDialog("Input FEN");
                undoAllMoves();
                chessBoard = FenUtilities.createGameFromFEN(fenString);
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });
        filesMenu.add(openFEN);

        final JMenuItem saveToPGN = new JMenuItem("Save Game", KeyEvent.VK_S);
        saveToPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return ".pgn";
                    }
                    @Override
                    public boolean accept(final File file) {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith("pgn");
                    }
                });
                final int option = chooser.showSaveDialog(Table.get().getGameFrame());
                if (option == JFileChooser.APPROVE_OPTION) {
                    savePGNFile(chooser.getSelectedFile());
                }
            }
        });
        filesMenu.add(saveToPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                exit();
            }
        });
        filesMenu.add(exitMenuItem);

        return filesMenu;
    }

    private JMenu createOptionsMenu() {

        final JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);

        final JMenuItem resetMenuItem = new JMenuItem("New Game", KeyEvent.VK_P);
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                undoAllMoves();
            }

        });
        optionsMenu.add(resetMenuItem);

        final JMenuItem evaluateBoardMenuItem = new JMenuItem("Evaluate Board", KeyEvent.VK_E);
        evaluateBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println(StandardBoardEvaluator.get().evaluate(chessBoard, gameSetup.getSearchDepth()));

            }
        });
        optionsMenu.add(evaluateBoardMenuItem);

        final JMenuItem escapeAnalysis = new JMenuItem("Escape Analysis Score", KeyEvent.VK_S);
        escapeAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Move lastMove = moveLog.getMoves().get(moveLog.size() - 1);
                if(lastMove != null) {
                    System.out.println(MoveUtils.exchangeScore(lastMove));
                }

            }
        });
        optionsMenu.add(escapeAnalysis);

        final JMenuItem legalMovesMenuItem = new JMenuItem("Current State", KeyEvent.VK_L);
        legalMovesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println(chessBoard.getWhitePieces());
                System.out.println(chessBoard.getBlackPieces());
                System.out.println(playerInfo(chessBoard.currentPlayer()));
                System.out.println(playerInfo(chessBoard.currentPlayer().getOpponent()));
            }
        });
        optionsMenu.add(legalMovesMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move", KeyEvent.VK_M);
        undoMoveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if(Table.get().getMoveLog().size() > 0) {
                    undoLastMove();
                }
            }
        });
        optionsMenu.add(undoMoveMenuItem);

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game", KeyEvent.VK_S);
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }

    private JMenu createPreferencesMenu() {

        final JMenu preferencesMenu = new JMenu("Preferences");

        final JMenu colorChooserSubMenu = new JMenu("Choose Colors");
        colorChooserSubMenu.setMnemonic(KeyEvent.VK_S);

        final JMenuItem chooseDarkMenuItem = new JMenuItem("Choose Dark Tile Color");
        colorChooserSubMenu.add(chooseDarkMenuItem);

        final JMenuItem chooseLightMenuItem = new JMenuItem("Choose Light Tile Color");
        colorChooserSubMenu.add(chooseLightMenuItem);

        final JMenuItem chooseLegalHighlightMenuItem = new JMenuItem(
                "Choose Legal Move Highlight Color");
        colorChooserSubMenu.add(chooseLegalHighlightMenuItem);

        preferencesMenu.add(colorChooserSubMenu);

        chooseDarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Color colorChoice = JColorChooser.showDialog(Table.get().getGameFrame(), "Choose Dark Tile Color",
                        Table.get().getGameFrame().getBackground());
                if (colorChoice != null) {
                    Table.get().getBoardPanel().setTileDarkColor(chessBoard, colorChoice);
                }
            }
        });

        chooseLightMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Color colorChoice = JColorChooser.showDialog(Table.get().getGameFrame(), "Choose Light Tile Color",
                        Table.get().getGameFrame().getBackground());
                if (colorChoice != null) {
                    Table.get().getBoardPanel().setTileLightColor(chessBoard, colorChoice);
                }
            }
        });

        final JMenu chessMenChoiceSubMenu = new JMenu("Choose Chess Men Image Set");

        final JMenuItem holyWarriorsMenuItem = new JMenuItem("Holy Warriors");
        chessMenChoiceSubMenu.add(holyWarriorsMenuItem);

        final JMenuItem rockMenMenuItem = new JMenuItem("Rock Men");
        chessMenChoiceSubMenu.add(rockMenMenuItem);

        final JMenuItem abstractMenMenuItem = new JMenuItem("Abstract Men");
        chessMenChoiceSubMenu.add(abstractMenMenuItem);

        final JMenuItem woodMenMenuItem = new JMenuItem("Wood Men");
        chessMenChoiceSubMenu.add(woodMenMenuItem);

        final JMenuItem fancyMenMenuItem = new JMenuItem("Fancy Men");
        chessMenChoiceSubMenu.add(fancyMenMenuItem);

        final JMenuItem fancyMenMenuItem2 = new JMenuItem("Fancy Men 2");
        chessMenChoiceSubMenu.add(fancyMenMenuItem2);

        woodMenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println("implement me");
                Table.get().getGameFrame().repaint();
            }
        });

        holyWarriorsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                pieceIconPath = "art/holywarriors/";
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });

        rockMenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        });

        abstractMenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                pieceIconPath = "art/simple/";
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });

        fancyMenMenuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                pieceIconPath = "art/fancy2/";
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });

        fancyMenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                pieceIconPath = "art/fancy/";
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });

        preferencesMenu.add(chessMenChoiceSubMenu);

        chooseLegalHighlightMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println("implement me");
                Table.get().getGameFrame().repaint();
            }
        });

        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip board");

        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });

        preferencesMenu.add(flipBoardMenuItem);
        preferencesMenu.addSeparator();


        final JCheckBoxMenuItem cbLegalMoveHighlighter = new JCheckBoxMenuItem(
                "Highlight Legal Moves", false);

        cbLegalMoveHighlighter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                highlightLegalMoves = cbLegalMoveHighlighter.isSelected();
            }
        });

        preferencesMenu.add(cbLegalMoveHighlighter);

        final JCheckBoxMenuItem cbUseBookMoves = new JCheckBoxMenuItem(
                "Use Book Moves", false);

        cbUseBookMoves.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                useBook = cbUseBookMoves.isSelected();
            }
        });

        preferencesMenu.add(cbUseBookMoves);

        return preferencesMenu;

    }

    private static String playerInfo(final Player player) {
        return ("Player is: " +player.getAlliance() + "\nlegal moves (" +player.getLegalMoves().size()+ ") = " +player.getLegalMoves() + "\ninCheck = " +
                player.isInCheck() + "\nisInCheckMate = " +player.isInCheckMate() +
                "\nisCastled = " +player.isCastled())+ "\n";
    }

    private void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    private void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private void undoAllMoves() {
        for(int i = Table.get().getMoveLog().size() - 1; i >= 0; i--) {
            final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
            this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        }
        this.computerMove = null;
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
        Table.get().getDebugPanel().redo();
        whiteUsebook = true;
        blackUsebook = true;
        gameBoards.clear();
        repeatedBoards.clear();
        whiteAktualRepeatedBoards.clear();
        blackAktualRepeatedBoards.clear();
    }

    private static void loadPGNFile(final File pgnFile) {
        try {
            persistPGNFile(pgnFile);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void savePGNFile(final File pgnFile) {
        try {
            writeGameToPGNFile(pgnFile, Table.get().getMoveLog());
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void undoLastMove() {
        final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
        gameBoards.remove(gameBoards.size() - 1);
        removeFromRepetaetedBordsIfNeeded(chessBoard);
        updateActualRepeatedBoards();
        this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        changeInRepetitionBoardsIfNeeded(chessBoard);
        updateActualRepeatedBoards();
        this.computerMove = null;
        Table.get().getMoveLog().removeMove(lastMove);
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
        Table.get().getDebugPanel().redo();
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private void setupUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    public boolean isRepeatedBoard(Board board) {
        List<Board> aktualRepeatedBoards = board.currentPlayer().getAlliance() == Alliance.WHITE ?
                whiteAktualRepeatedBoards : blackAktualRepeatedBoards;
        for (Board b : aktualRepeatedBoards) {
            if (BoardEvaluator.equalsForRepetition(b, board)) {
                return true;
            }
        }

        return false;
    }

    private static class TableGameAIWatcher
            implements Observer {

        @Override
        public void update(final Observable o,
                           final Object arg) {

            if (Table.get().getGameBoard().currentPlayer().isInCheckMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in checkmate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in stalemate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Table.get().updateActualRepeatedBoards();

            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer())) {
                System.out.println(Table.get().getGameBoard().currentPlayer() + " is set to AI, thinking....");
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if (Table.get().isRepeatedBoard(Table.get().getGameBoard())) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: draw by three fold repetition!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        }

    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {
        }

        @Override
        protected Move doInBackground() throws Exception {
//            Table.get().updateActualRepeatedBoards();
            final Move bestMove;
            final Move bookMove = Table.get().getUseBook()
                    ? MySqlGamePersistence.get().getNextBestMove(Table.get().getGameBoard(),
                    Table.get().getGameBoard().currentPlayer(),
                    Table.get().getMoveLog().getMoves().toString().replaceAll("\\[", "").replaceAll("\\]", ""))
                    : MoveFactory.getNullMove();
            if (Table.get().getUseBook() && bookMove != MoveFactory.getNullMove()) {
                bestMove = bookMove;
            }
            else {
                if (Table.get().getGameBoard().currentPlayer().getAlliance() == Alliance.WHITE) {
                    Table.get().whiteUsebook = false;
                } else {
                    Table.get().blackUsebook = false;
                }
                AtomicInteger pieceValues = new AtomicInteger(0);
                Table.get().getGameBoard().getWhitePieces().stream().filter(p -> p.getPieceType() != PieceType.KING)
                        .forEach(p -> pieceValues.addAndGet(p.getPieceValue()));
                Table.get().getGameBoard().getBlackPieces().stream().filter(p -> p.getPieceType() != PieceType.KING)
                        .forEach(p -> pieceValues.addAndGet(p.getPieceValue()));
//                int numPieces = Table.get().getGameBoard().getWhitePieces().size() +
//                        Table.get().getGameBoard().getBlackPieces().size();
//                //int bonusDepth = Math.min(4, 1 + Math.round((float)32/numPieces));
                int bonusDepth = pieceValues.get() <= 600 ? 4 : pieceValues.get() <= 3300 ? 2 : 0;
                final MoveStrategy strategy =
                        new StockAlphaBeta(Table.get().getGameSetup().getSearchDepth() + bonusDepth, Table.get().getDebugPanel(),
                                Table.get().whiteAktualRepeatedBoards, Table.get().blackAktualRepeatedBoards);
                bestMove = strategy.execute(
                        Table.get().getGameBoard());
            }
            return bestMove;
        }

        @Override
        public void done() {
            try {
                final Move bestMove = get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getToBoard());
                Table.get().gameBoards.add(Table.get().getGameBoard());
                Table.get().addToRepetaetedBordsIfNeeded(Table.get().getGameBoard());
                Table.get().updateActualRepeatedBoards();
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().getDebugPanel().redo();
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BoardPanel extends JPanel {

        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8,8));
            this.boardTiles = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    final TilePanel tilePanel = new TilePanel(this, index);
                    this.boardTiles.add(tilePanel);
                    add(tilePanel);
                    index++;
                }
                index += 8;
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(Color.decode("#8B4726"));
            validate();
        }

        void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel boardTile : boardDirection.traverse(boardTiles)) {
                boardTile.drawTile(board);
                add(boardTile);
            }
            validate();
            repaint();
        }

        void setTileDarkColor(final Board board,
                              final Color darkColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setDarkTileColor(darkColor);
            }
            drawBoard(board);
        }

        void setTileLightColor(final Board board,
                                      final Color lightColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setLightTileColor(lightColor);
            }
            drawBoard(board);
        }

    }

    enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();

    }

    public static class MoveLog {

        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        void clear() {
            this.moves.clear();
        }

        Move removeMove(final int index) {
            return this.moves.remove(index);
        }

        boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }

    }

    private class TilePanel extends JPanel {

        private final int tileId;

        TilePanel(final BoardPanel boardPanel,
                  final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);
            highlightTileBorder(chessBoard);
            addMouseListener(new MouseListener() {
                @Override
                public void mousePressed(final MouseEvent event) {

                    if(Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) ||
                        isEndGame()) {
                        return;
                    }

                    if (isRightMouseButton(event)) {
                        sourceTile = null;
                        humanMovedPiece = null;
                    } else if (isLeftMouseButton(event)) {
                        if (sourceTile == null) {
                            sourceTile = chessBoard.getPiece(tileId);
                            humanMovedPiece = sourceTile;
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        } else {
                            final Move move = MoveFactory.createMove(chessBoard, sourceTile.getPosition(),
                                    tileId);
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getToBoard();
                                gameBoards.add(chessBoard);
                                addToRepetaetedBordsIfNeeded(chessBoard);
//                                updateActualRepeatedBoards();
                                moveLog.addMove(move);
                            }
                            sourceTile = null;
                            humanMovedPiece = null;
                        }
                    }
                    invokeLater(new Runnable() {
                        public void run() {
                            gameHistoryPanel.redo(chessBoard, moveLog);
                            takenPiecesPanel.redo(moveLog);
                            //if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            //}
                            boardPanel.drawBoard(chessBoard);
                            debugPanel.redo();
                        }
                    });
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                }

                @Override
                public void mouseEntered(final MouseEvent e) {
                }

                @Override
                public void mouseReleased(final MouseEvent e) {
                }

                @Override
                public void mouseClicked(final MouseEvent e) {
                }
            });
            validate();
        }

        void drawTile(final Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightTileBorder(board);
            highlightLegals(board);
            highlightAIMove();
            validate();
            repaint();
        }

        void setLightTileColor(final Color color) {
            lightTileColor = color;
        }

        void setDarkTileColor(final Color color) {
            darkTileColor = color;
        }

        private void highlightTileBorder(final Board board) {
            if(humanMovedPiece != null &&
               humanMovedPiece.getAlliance() == board.currentPlayer().getAlliance() &&
               humanMovedPiece.getPosition() == this.tileId) {
                setBorder(BorderFactory.createLineBorder(Color.cyan));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }

        private void highlightAIMove() {
            if(computerMove != null) {
                if(this.tileId == computerMove.getCurrentCoordinate()) {
                    setBackground(Color.pink);
                } else if(this.tileId == computerMove.getDestinationCoordinate()) {
                    setBackground(Color.red);
                }
            }
        }

        private void highlightLegals(final Board board) {
            if (Table.get().getHighlightLegalMoves()) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileId) {
                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        }
                        catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private List<Move> pieceLegalMoves(final Board board) {
            if(humanMovedPiece != null && humanMovedPiece.getAlliance() == board.currentPlayer().getAlliance()) {
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if(board.getPiece(this.tileId) != null) {
                try{
                    final BufferedImage image = ImageIO.read(new File(pieceIconPath +
                            board.getPiece(this.tileId).getAlliance().toString().substring(0, 1) + "" +
                            board.getPiece(this.tileId).toString() +
                            ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch(final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void assignTileColor() {
            if ((this.tileId & 0x10) == 0) {
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            } else {
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }

    private void addToRepetaetedBordsIfNeeded(Board board) {
        for (Board b : gameBoards) {
            if (BoardEvaluator.equalsForRepetition(b, board)) {
                if (!containsForRepetation(board)) {
                    repeatedBoards.add(board);
                }
                break;
            }
        }
    }

    private void removeFromRepetaetedBordsIfNeeded(Board board) {
        if (repeatedBoards.isEmpty()) {
            return;
        }
        Board b = repeatedBoards.get(repeatedBoards.size() - 1);
        if (b == board || BoardEvaluator.equalsForRepetition(b, board)) {
            repeatedBoards.remove(repeatedBoards.size() - 1);
        }
    }

    private void changeInRepetitionBoardsIfNeeded(Board board) {
        for (int i = 0; i < repeatedBoards.size(); i++) {
            if (BoardEvaluator.equalsForRepetition(board, repeatedBoards.get(i))) {
                repeatedBoards.set(i, board);
                break;
            }
        }
    }

    private boolean containsForRepetation(Board board) {
        for (Board b : repeatedBoards) {
            if (b == board || BoardEvaluator.equalsForRepetition(b, board)) {
                return true;
            }
        }

        return false;
    }

    public void updateActualRepeatedBoards() {
        updateActualRepeatedBoards(whiteAktualRepeatedBoards, Alliance.WHITE);
        updateActualRepeatedBoards(blackAktualRepeatedBoards, Alliance.BLACK);
    }

    private void updateActualRepeatedBoards(List<Board> aktualRepeatedBoards, Alliance alliance) {
        aktualRepeatedBoards.clear();
        for (Board b : repeatedBoards) {
            if (b.currentPlayer().getAlliance() == alliance && BoardEvaluator.piecesEquals(b, chessBoard)) {
                aktualRepeatedBoards.add(b);
            }
        }
    }

    public boolean isEndGame() {
        return chessBoard.currentPlayer().isInCheckMate() ||
               chessBoard.currentPlayer().isInStaleMate() ||
               BoardEvaluator.isRepeatedBoard(chessBoard, whiteAktualRepeatedBoards, blackAktualRepeatedBoards);
    }
}

