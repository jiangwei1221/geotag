package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:test.db", "", "");
		connection.setAutoCommit(false);
		return connection;
	}


}
