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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.crawler.node.FollowerCrawler;
import com.buddycloud.channeldirectory.crawler.node.MetaDataCrawler;
import com.buddycloud.channeldirectory.crawler.node.NodeCrawler;
import com.buddycloud.channeldirectory.crawler.node.PostCrawler;

/**
 * Responsible for regularly querying servers for
 * its nodes, subscribing to them and invoking the
 * NodeCrawlers.
 * 
 */
public class PubSubServerCrawler {

	private static Logger LOGGER = Logger.getLogger(PubSubServerCrawler.class);
	
	private final Properties configuration;
	private final PubSubManagers managers;
	private final PubSubSubscriptionListener listener;
	private final ChannelDirectoryDataSource dataSource;
	
	public PubSubServerCrawler(Properties configuration, PubSubManagers managers, 
			ChannelDirectoryDataSource dataSource, PubSubSubscriptionListener listener) {
		this.configuration = configuration;
		this.managers = managers;
		this.dataSource = dataSource;
		this.listener = listener;
	}
	
	public void start() throws XMPPException {
		
		String serversToCrawlStr = configuration.getProperty("crawler.servertocrawl");
		String[] serversToCrawl = serversToCrawlStr.split(",");
		
		List<NodeCrawler> nodeCrawlers = new LinkedList<NodeCrawler>();
		nodeCrawlers.add(new MetaDataCrawler(configuration, dataSource));
		nodeCrawlers.add(new PostCrawler(configuration));
		nodeCrawlers.add(new FollowerCrawler(dataSource));
		
		while (true) {
			
			for (String server : serversToCrawl) {
				PubSubManager manager = managers.getPubSubManager(server);
				DiscoverItems discoverInfo = null;
				try {
					discoverInfo = manager.discoverNodes(null);
				} catch (Exception e) {
					LOGGER.warn("Could not fetch nodes from server [" + server + "]", e);
					continue;
				}
				
				try {
					Node firehoseNode = manager.getNode("firehose");
					listener.listen(firehoseNode, server);
				} catch (Exception e) {
					LOGGER.warn("Could not subscribe to firehose node from server [" + server + "]", e);
				}
				
				if (discoverInfo == null) {
					continue;
				}
				
				Iterator<Item> idsIterator = discoverInfo.getItems();
				
				while (idsIterator.hasNext()) {
					Item item = idsIterator.next();
					for (NodeCrawler nodeCrawler : nodeCrawlers) {
						try {
							Node node = manager.getNode(item.getName());
							nodeCrawler.crawl(node, server);
							insertNode(node, server);
						} catch (Exception e) {
							LOGGER.warn("Could not crawl node [" + item.getName() + "] " +
									"from server [" + server + "]", e);
						}
					}
				}
			}
			
			try {
				Thread.sleep(60000 * 5);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void insertNode(Node node, String server) throws SQLException {
		
		PreparedStatement statement = dataSource.prepareStatement(
				"INSERT INTO subscribed_node(name, server) values (?, ?)", 
				node.getId(), server);
		statement.setString(0, node.getId());
		statement.setString(1, server);
		
		try {
			statement.execute();
		} catch (SQLException e) {
			LOGGER.warn("Node already subscribed " + node + " " + server);
		} finally {
			statement.close();
			statement.getConnection().close();
		}
	}
	
}
