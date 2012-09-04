/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.channeldirectory.commons.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Handles requests related to the PostgreSQL data source,
 * that stores crawled nodes and their update times.
 * It uses a {@link ComboPooledDataSource} to improve connection
 * pooling.
 * 
 */
public class ChannelDirectoryDataSource {

	private ComboPooledDataSource dataSource;
	private Properties configuration;

	public ChannelDirectoryDataSource(Properties configuration) throws PropertyVetoException {
		this.configuration = configuration;
		createDataSource();
	}

	public Statement createStatement() throws SQLException {
		return dataSource.getConnection().createStatement();
	}
	
	public PreparedStatement prepareStatement(String statement, Object... args) throws SQLException {
		PreparedStatement prepareStatement = dataSource.getConnection().prepareStatement(statement);
		for (int i = 1; i <= args.length; i++) {
			prepareStatement.setObject(i, args[i-1]);
		}
		return prepareStatement;
	}
	
	public static void close(Statement statement) {
		
		if (statement == null) {
			return;
		}
		
		try {
			Connection connection = statement.getConnection();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	private void createDataSource() throws PropertyVetoException {
		this.dataSource = new ComboPooledDataSource();
		dataSource.setMaxPoolSize(10);
		dataSource.setDriverClass("org.postgresql.Driver");
		dataSource.setJdbcUrl(configuration.getProperty("mahout.jdbc.url"));
	}
	
	/**
	 * @return the dataSource
	 */
	public ComboPooledDataSource getDataSource() {
		return dataSource;
	}
}
