package org.seng302.datagenerator;

import java.sql.*;

public class Main {
    /**
     * Connects to Marinadb production environment
     * @return the connection to the database
     */
    public static Connection connectToDatabase() throws SQLException {
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        Connection conn = DriverManager.getConnection(url, "seng302-team500", "ListenDirectly6053");
        return conn;
    }

    /**
     * Clears the console on windows and linux
     */
    public static void clear() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }
}