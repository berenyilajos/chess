package hu.berenyilajos.chess.pgn;

import hu.berenyilajos.chess.board.Board;
import hu.berenyilajos.chess.board.Move;
import hu.berenyilajos.chess.pieces.Alliance;
import hu.berenyilajos.chess.player.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGamePersistence implements PGNPersistence {

    private final Connection dbConnection;

    private static MySqlGamePersistence INSTANCE = new MySqlGamePersistence();
    private static final int MAX_BEST_MOVES = 10;
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final String DB_FILE_PATH = "db/chess.db";
    private static final String DB_PATH = "jdbc:sqlite:" + DB_FILE_PATH;
    //    private static final String DB_URL = "jdbc:mysql://localhost/chessgames";
//    private static final String USER = "root";
//    private static final String PASS = "nyw";
    private static final String NEXT_BEST_MOVE_QUERY =
            "SELECT SUBSTR(moves, %d, INSTR(SUBSTR(moves, %d, LENGTH(moves)), ',') - 1) move, " +
                    "COUNT(*) FROM Game WHERE moves LIKE '%s%%' AND outcome = '%s' GROUP BY move ORDER BY 2 DESC";


    private MySqlGamePersistence() {
        this.dbConnection = createDBConnection();
        createGameTable();
        createIndex("outcome", "OutcomeIndex");
        createIndex("moves", "MoveIndex");
//        createOutcomeIndex();
//        createMovesIndex();
    }

    private static Connection createDBConnection() {
        File fajl = new File(DB_FILE_PATH).getAbsoluteFile();
        fajl.setReadable(true, false);
        fajl.setWritable(true, false);
        try {
            Class.forName(JDBC_DRIVER);
            return DriverManager.getConnection(DB_PATH);
        }
        catch (final ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MySqlGamePersistence get() {
        return INSTANCE;
    }

    @Override
    public void persistGame(final Game game) {
        executePersist(game);
    }

    @Override
    public Move getNextBestMove(final Board board,
                                final Player player,
                                final String gameText) {
        return queryBestMove(board, player, gameText);
    }

    private Move queryBestMove(final Board board,
                               final Player player,
                               final String gameText) {

        String bestMove = "";
        if (gameText.length() > 150) {
            return PGNUtilities.createMove(board, bestMove);
        }
        int count = 0;
        try {
            final int offSet = gameText.isEmpty() ? 1 : 3;
            int length = gameText.length() + offSet;
            final String sqlString = String.format(NEXT_BEST_MOVE_QUERY, length, length, gameText,
                    player.isWhite() ? Alliance.WHITE : Alliance.BLACK);
            System.out.println(sqlString);
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet rs2 = gameStatement.getResultSet();
            int sum = 0;
            List<String> bestMoves = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            int index = 0;
            while(rs2.next() && index < MAX_BEST_MOVES) {
                bestMoves.add(rs2.getString(1));
                counts.add(rs2.getInt(2));
                sum += counts.get(index);
                index++;
            }
            if (sum > 0) {
                int randomBest = (int) (Math.random() * sum);
                sum = 0;
                index = 0;
                while ((sum += counts.get(index)) <= randomBest) {
                    index++;
                }
                count = counts.get(index);
                bestMove = bestMoves.get(index).trim();
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\tselected book move = " +bestMove+ " with " +count+ " hits");
        return PGNUtilities.createMove(board, bestMove);
    }

    private void createGameTable() {
        try {
            final Statement statement = this.dbConnection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Game(id int not null, outcome varchar(10), moves varchar(4000), primary key (id));");
            statement.execute("PRAGMA encoding = 'UTF-8'");
            statement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private void createIndex(final String columnName,
                             final String indexName) {
        try {
//            final String sqlString = "SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_CATALOG = 'def' AND " +
//                    "                 TABLE_SCHEMA = DATABASE() AND TABLE_NAME = \"game\" AND INDEX_NAME = \"" +indexName+"\"";
//            final Statement gameStatement = this.dbConnection.createStatement();
//            gameStatement.execute(sqlString);
//            final ResultSet resultSet = gameStatement.getResultSet();
//            if(!resultSet.isBeforeFirst() ) {
            final Statement indexStatement = this.dbConnection.createStatement();
            indexStatement.execute("CREATE INDEX IF NOT EXISTS " +indexName+ " on Game(" +columnName+ ");\n");
            indexStatement.close();
//            }
//            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }

    }

    private void createOutcomeIndex() {
        try {
            final String sqlString = "SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_CATALOG = 'def' AND TABLE_SCHEMA = DATABASE() AND TABLE_NAME = \"game\" AND INDEX_NAME = \"OutcomeIndex\"";
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet resultSet = gameStatement.getResultSet();
            if(!resultSet.isBeforeFirst() ) {
                final Statement indexStatement = this.dbConnection.createStatement();
                indexStatement.execute("CREATE INDEX OutcomeIndex on Game(outcome);\n");
                indexStatement.close();
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private void createMovesIndex() {
        try {
            final String sqlString = "SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_CATALOG = 'def' AND TABLE_SCHEMA = DATABASE() AND TABLE_NAME = \"game\" AND INDEX_NAME = \"MoveIndex\"";
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet resultSet = gameStatement.getResultSet();
            if(!resultSet.isBeforeFirst() ) {
                final Statement indexStatement = this.dbConnection.createStatement();
                indexStatement.execute("CREATE INDEX MoveIndex on Game(moves);\n");
                indexStatement.close();
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxGameRow() {
        int maxId = 0;
        try {
            final String sqlString = "SELECT MAX(ID) FROM Game";
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet rs2 = gameStatement.getResultSet();
            if(rs2.next()) {
                maxId = rs2.getInt(1);
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    private void executePersist(final Game game) {
        try {
            final String gameSqlString = "INSERT INTO Game(id, outcome, moves) VALUES(?, ?, ?);";
            final PreparedStatement gameStatement = this.dbConnection.prepareStatement(gameSqlString);
            int gameRow = getMaxGameRow() + 1;
            String winner = game.getWinner();
            String rawMoves = game.getMoves().toString();
            String moves = rawMoves.replaceAll("\\[", "").replaceAll("\\]", "");
            gameStatement.setInt(1, gameRow);
            gameStatement.setString(2, winner);
            gameStatement.setString(3, moves);
            gameStatement.executeUpdate();
            gameStatement.close();
//            System.err.println("gameRow=" + gameRow + ", winner=" + winner + ", moves=" + moves);
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

}
