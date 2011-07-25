package com.buddycloud.channeldirectory.handler.common.mahout;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;

import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PostgreSQLRecommenderDataModel implements ChannelRecommenderDataModel {

	private final Properties properties;
	private ComboPooledDataSource dataSource;
	private DataModel dataModel;

	public PostgreSQLRecommenderDataModel(Properties properties) {
		this.properties = properties;
		try {
			createDataSource();
			createDataModel();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createDataModel() throws TasteException {
		this.dataModel = new ReloadFromJDBCDataModel(
				new PostgreSQLBooleanPrefJDBCDataModel(
				dataSource));
	}
	
	private void createDataSource() throws PropertyVetoException {
		this.dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("org.postgresql.Driver");
		dataSource.setJdbcUrl(properties.getProperty("mahout.jdbc.url"));
	}

	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public long toUserId(String userJid) {
		try {
			Connection connection = dataSource.getConnection();
			PreparedStatement selectUserSt = connection
					.prepareStatement("SELECT id FROM t_user WHERE jid  = ?");
			selectUserSt.setString(1, userJid);

			ResultSet resultSet = selectUserSt.executeQuery();
			resultSet.next();

			long userId = resultSet.getLong("id");
			selectUserSt.close();
			connection.close();
			
			return userId;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ChannelData toChannelData(long itemID) {
		try {
			Connection connection = dataSource.getConnection();
			
			PreparedStatement selectItemSt = connection
					.prepareStatement("SELECT jid, title, description FROM item WHERE id = ?");
			
			selectItemSt.setLong(1, itemID);
			
			ResultSet resultSet = selectItemSt.executeQuery();
			resultSet.next();
			
			String jid = resultSet.getString("jid");
			String title = resultSet.getString("title");
			String desc = resultSet.getString("description");
			
			selectItemSt.close();
			
			ChannelData channelData = new ChannelData();
			channelData.setId(jid);
			channelData.setTitle(title);
			channelData.setDescription(desc);
			
			connection.close();
			
			return channelData;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long toChannelId(String channelJid) {
		try {
			Connection connection = dataSource.getConnection();

			PreparedStatement selectItemSt = connection
					.prepareStatement("SELECT id FROM item WHERE jid  = ?");
			selectItemSt.setString(1, channelJid);

			ResultSet resultSet = selectItemSt.executeQuery();
			resultSet.next();

			long itemId = resultSet.getLong("id");
			selectItemSt.close();
			connection.close();

			return itemId;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
