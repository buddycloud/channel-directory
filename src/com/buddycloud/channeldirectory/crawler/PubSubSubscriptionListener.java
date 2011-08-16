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

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.ConfigurationEvent;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemDeleteEvent;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.listener.ItemDeleteListener;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.listener.NodeConfigListener;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;

/**
 *
 */
public class PubSubSubscriptionListener implements ItemDeleteListener, ItemEventListener<Item>, NodeConfigListener {

	private static Logger LOGGER = Logger.getLogger(PubSubServerCrawler.class);
	
	private final PubSubManagers managers;
	private final ChannelDirectoryDataSource dataSource;
	private String userId;


	/**
	 * @param configuration
	 * @param managers
	 */
	public PubSubSubscriptionListener(Properties configuration,
			PubSubManagers managers, ChannelDirectoryDataSource dataSource) {
		this.managers = managers;
		this.dataSource = dataSource;
		
		String userName = configuration.getProperty("crawler.xmpp.username");
		String serverName = configuration.getProperty("crawler.xmpp.servername");
		
		this.userId = userName + "@" + serverName;
		
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.NodeConfigListener#handleNodeConfiguration(org.jivesoftware.smackx.pubsub.ConfigurationEvent)
	 */
	@Override
	public void handleNodeConfiguration(ConfigurationEvent config) {
		ConfigureForm configureForm = config.getConfiguration();
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.pubsub.listener.ItemEventListener#handlePublishedItems(org.jivesoftware.smackx.pubsub.ItemPublishEvent)
	 */
	@Override
	public void handlePublishedItems(ItemPublishEvent<Item> itemsEvent) {
		List<Item> items = itemsEvent.getItems();
		for (Item item : items) {
			String itemId = item.getId();
		}
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
	 * @param node
	 * @throws XMPPException 
	 * @throws SQLException 
	 */
	public void listen(Node node, String server) throws XMPPException, SQLException {
		node.subscribe(userId);
		node.addConfigurationListener(this);
		node.addItemDeleteListener(this);
		node.addItemEventListener(this);
	}
	
}
