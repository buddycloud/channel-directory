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
package com.buddycloud.channeldirectory.search.handler.common.mahout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;

/**
 * Reads taste data from a PostgreSQL database and stores
 * it in memory. Whenever the database is updated, the data model
 * reloads the updated data. 
 * 
 */
public class PostgreSQLRecommenderDataModel implements ChannelRecommenderDataModel {

	private ChannelDirectoryDataSource dataSource;
	private DataModel dataModel;

	public PostgreSQLRecommenderDataModel(Properties properties) {
		try {
			dataSource = new ChannelDirectoryDataSource(properties);
			createDataModel();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createDataModel() throws TasteException {
		this.dataModel = new ReloadFromJDBCDataModel(
				new PostgreSQLBooleanPrefJDBCDataModel(
				dataSource.getDataSource()));
	}

	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public long toUserId(String userJid) {
		
		PreparedStatement statement = null;
		
		try {
			Connection connection = dataSource.getConnection();
			statement = connection
					.prepareStatement("SELECT id FROM t_user WHERE jid  = ?");
			statement.setString(1, userJid);

			ResultSet resultSet = statement.executeQuery();
			resultSet.next();

			long userId = resultSet.getLong("id");

			return userId;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(statement);
		}
	}

	/**
	 * @param statement
	 */
	private void close(PreparedStatement statement) {
		if (statement == null) {
			return;
		}
		
		try {
			Connection connection = statement.getConnection();
			statement.close();
			if (connection != null) {
				connection.close();
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ChannelData toChannelData(long itemID) {
		
		PreparedStatement statement = null;
		
		try {
			Connection connection = dataSource.getConnection();
			
			statement = connection
					.prepareStatement("SELECT jid, title, description FROM item WHERE id = ?");
			
			statement.setLong(1, itemID);
			
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			
			String jid = resultSet.getString("jid");
			String title = resultSet.getString("title");
			String desc = resultSet.getString("description");
			
			ChannelData channelData = new ChannelData();
			channelData.setId(jid);
			channelData.setTitle(title);
			channelData.setDescription(desc);
			
			return channelData;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public long toChannelId(String channelJid) {
		
		PreparedStatement statement = null;
		
		try {
			Connection connection = dataSource.getConnection();

			statement = connection
					.prepareStatement("SELECT id FROM item WHERE jid  = ?");
			statement.setString(1, channelJid);

			ResultSet resultSet = statement.executeQuery();
			resultSet.next();

			long itemId = resultSet.getLong("id");

			return itemId;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(statement);
		}
	}

}
