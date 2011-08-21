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
package com.buddycloud.channeldirectory.crawler.node;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.Subscription;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;

/**
 * Responsible for crawling {@link Node} data
 * regarding subscribers. 
 * 
 */
public class FollowerCrawler implements NodeCrawler {

	private static Logger LOGGER = Logger.getLogger(FollowerCrawler.class);
	
	private final ChannelDirectoryDataSource dataSource;
	
	public FollowerCrawler(ChannelDirectoryDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#crawl(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public void crawl(Node node, String server) throws Exception {
		List<Subscription> subscriptions = node.getSubscriptions();
		
		String item = node.getId();
		
		for (Subscription subscription : subscriptions) {
		
			String user = subscription.getJid();
			
			enqueueNewServer(user);
			
			Connection connection = dataSource.getConnection();
			
			Long userId = fetchRowId(user, "t_user", connection);
			Long itemId = fetchRowId(item, "item", connection);
			
			Statement selectTasteSt = connection.createStatement();
			ResultSet selectTasteResult = selectTasteSt.executeQuery(
					"SELECT * FROM taste_preferences WHERE user_id = '" + userId + "' " +
					"AND item_id = '" + itemId + "'");
			
			if (!selectTasteResult.next()) {
				Statement insertTasteSt = connection.createStatement();
				insertTasteSt.execute("INSERT INTO taste_preferences(user_id, item_id) " +
						"VALUES ('" + userId + "', '" + itemId + "')");
				insertTasteSt.close();
			}
			
			ChannelDirectoryDataSource.close(selectTasteSt);
		}
	
		try {
			updateSubscribedNode(node.getId(), server);
		} catch (SQLException e1) {
			LOGGER.warn("Could not update subscribed node", e1);
		}
		
	}

	/**
	 * @param user
	 * @throws SQLException 
	 */
	private void enqueueNewServer(String user) throws SQLException {
		String server = user.substring(user.indexOf('@'));
		
		PreparedStatement statement = dataSource.prepareStatement(
				"INSERT INTO subscribed_server(name) values (?)", 
				server);
		
		try {
			statement.execute();
		} catch (SQLException e) {
			LOGGER.warn("Server already inserted " + server);
		} finally {
			ChannelDirectoryDataSource.close(statement);
		}
	}

	private void updateSubscribedNode(String nodeName, String server) throws SQLException {
		
		PreparedStatement prepareStatement = dataSource.prepareStatement(
				"UPDATE subscribed_node SET subscribers_updated = ? WHERE name = ? AND server = ?", 
				new Date(System.currentTimeMillis()), nodeName, server);
		prepareStatement.execute();
		ChannelDirectoryDataSource.close(prepareStatement);
	}
	
	private static Long fetchRowId(String user, String tableName, Connection connection)
			throws SQLException {
		
		Statement selectRowSt = connection.createStatement();
		ResultSet selectRowResult = selectRowSt.executeQuery(
				"SELECT id FROM " + tableName + " WHERE jid = '" + user + "'");
		Long userId = null;
		
		if (selectRowResult.next()) {
			userId = selectRowResult.getLong("id");
		} else {
			Statement insertRowSt = connection.createStatement();
			insertRowSt.execute("INSERT INTO " + tableName + "(jid) VALUES ('" + user + "') RETURNING id");
			
			ResultSet insertUserResult = insertRowSt.getResultSet();
			insertUserResult.next();
			userId = insertUserResult.getLong("id");
			
			ChannelDirectoryDataSource.close(insertRowSt);
		}
		
		ChannelDirectoryDataSource.close(selectRowSt);
		
		return userId;
	}

}
