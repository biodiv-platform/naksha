package com.strandls.naksha.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.strandls.naksha.NakshaConfig;

public abstract class DAOFactory {

	private static final String PROPERTY_URL = "db.url";
	private static final String PROPERTY_USERNAME = "db.username";
	private static final String PROPERTY_PASSWORD = "db.password";

	/**
	 * Returns a new DAOFactory instance for the given database name.
	 * 
	 * @return A new DAOFactory instance for the given database name.
	 */
	public static DAOFactory getInstance() {
		
		String url = NakshaConfig.getString(PROPERTY_URL);
		String password = NakshaConfig.getString(PROPERTY_PASSWORD);
		String username = NakshaConfig.getString(PROPERTY_USERNAME);

		return new DriverManagerDAOFactory(url, username, password);
	}

	/**
	 * Returns a connection to the database. Package private so that it can be used
	 * inside the DAO package only.
	 * 
	 * @return A connection to the database.
	 * @throws SQLException
	 *             If acquiring the connection fails.
	 */
	public abstract Connection getConnection() throws SQLException;
}

/**
 * The DriverManager based DAOFactory.
 */
class DriverManagerDAOFactory extends DAOFactory {
	private String url;
	private String username;
	private String password;

	DriverManagerDAOFactory(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}
}