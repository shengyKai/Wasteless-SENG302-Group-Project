package org.seng302.datagenerator;

import java.sql.*;

public class Main {
    /**
     * Connects to Marinadb production environment
     * @return the connection to the database
     */
    public static Connection connectToDatabase() throws SQLException {
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        Connection conn = DriverManager.getConnection(url, "seng302-team500", "changeMe");
        return conn;
    }
}