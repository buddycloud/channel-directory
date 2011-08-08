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
package com.buddycloud.channeldirectory.crawler;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.ConfigurationEvent;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemDeleteEvent;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.listener.ItemDeleteListener;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.listener.NodeConfigListener;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 */
public class PubSubSubscriptionListener implements ItemDeleteListener, ItemEventListener<Item>, NodeConfigListener {

	private static Logger LOGGER = Logger.getLogger(PubSubServerCrawler.class);
	
	private final Properties configuration;
	private final PubSubManagers managers;
	private String userId;
	private ComboPooledDataSource dataSource;

	/**
	 * @param configuration
	 * @param managers
	 */
	public PubSubSubscriptionListener(Properties configuration,
			PubSubManagers managers) {
		this.configuration = configuration;
		this.managers = managers;
		
		String userName = configuration.getProperty("crawler.xmpp.username");
		String serverName = configuration.getProperty("crawler.xmpp.servername");
		
		this.userId = userName + "@" + serverName;
		
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.NodeConfigListener#handleNodeConfiguration(org.jivesoftware.smackx.pubsub.ConfigurationEvent)
	 */
	@Override
	public void handleNodeConfiguration(ConfigurationEvent config) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.ItemEventListener#handlePublishedItems(org.jivesoftware.smackx.pubsub.ItemPublishEvent)
	 */
	@Override
	public void handlePublishedItems(ItemPublishEvent<Item> items) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.ItemDeleteListener#handleDeletedItems(org.jivesoftware.smackx.pubsub.ItemDeleteEvent)
	 */
	@Override
	public void handleDeletedItems(ItemDeleteEvent items) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.ItemDeleteListener#handlePurge()
	 */
	@Override
	public void handlePurge() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @throws SQLException 
	 * @throws PropertyVetoException 
	 * 
	 */
	public void start() throws SQLException, PropertyVetoException {
		
		createDataSource();
		
		Connection connection = dataSource.getConnection();

		Statement statement = connection.createStatement();
		statement.executeQuery("SELECT * FROM subscribed_node");

		ResultSet subscribedNodes = statement.getResultSet();
		
		while (subscribedNodes.next()) {
			String nodeName = subscribedNodes.getString("name");
			String nodeServer = subscribedNodes.getString("server");
			
			PubSubManager pubSubManager = managers.getPubSubManager(nodeServer);
			try {
				Node node = pubSubManager.getNode(nodeName);
				listen(node, nodeServer, false);
			} catch (XMPPException e) {
				LOGGER.error(e);
			}
		}
		
		subscribedNodes.close();
		statement.close();
		connection.close();
		
	}

	/**
	 * @param node
	 * @throws XMPPException 
	 * @throws SQLException 
	 */
	public void listen(Node node, String server, boolean subscribe) throws XMPPException, SQLException {
		
		if (subscribe) {
			
			Connection connection = dataSource.getConnection();
			
			PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO subscribed_node(name, server) values (?, ?)");
			statement.setString(0, node.getId());
			statement.setString(1, server);
			
			try {
				statement.execute();
				node.subscribe(userId);
			} catch (SQLException e) {
				LOGGER.warn("Node already subscribed", e);
			} finally {
				statement.close();
				connection.close();
			}
		}
		
		node.addConfigurationListener(this);
		node.addItemDeleteListener(this);
		node.addItemEventListener(this);
	}
	
	private void createDataSource() throws PropertyVetoException {
		this.dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("org.postgresql.Driver");
		dataSource.setJdbcUrl(configuration.getProperty("mahout.jdbc.url"));
	}
}
