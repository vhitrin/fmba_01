package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {

    public static final String DATABASE = "db.sqlite";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {

        if (connection == null) {
            createConnection();
        } else {
            if (connection.isClosed()) {
                createConnection();
            }
        }

        return connection;
    }

    private static void createConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
    }
}
