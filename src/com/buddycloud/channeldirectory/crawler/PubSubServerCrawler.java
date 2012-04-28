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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.packet.RSMSet;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.packet.SyncPacketSend;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.crawler.node.CrawlerHelper;
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
	private static long DEF_CRAWL_INTERVAL = 60000 * 30; // 30 minutes
	
	private final Properties configuration;
	private final PubSubManagers managers;
	private final PubSubSubscriptionListener listener;
	private final ChannelDirectoryDataSource dataSource;
	
	private List<NodeCrawler> nodeCrawlers;
	
	public PubSubServerCrawler(Properties configuration, PubSubManagers managers, 
			ChannelDirectoryDataSource dataSource, PubSubSubscriptionListener listener) {
		this.configuration = configuration;
		this.managers = managers;
		this.dataSource = dataSource;
		this.listener = listener;
	}
	
	public void start() {
		
		this.nodeCrawlers = new LinkedList<NodeCrawler>();
		nodeCrawlers.add(new MetaDataCrawler(configuration, dataSource));
		nodeCrawlers.add(new PostCrawler(configuration));
		nodeCrawlers.add(new FollowerCrawler(dataSource));
		
		try {
			insertServers();
		} catch (SQLException e1) {
			LOGGER.error(e1);
		}
		
		String crawlIntervalStr = configuration.getProperty("crawler.crawlinterval");
		
		long crawlInterval = crawlIntervalStr == null ? DEF_CRAWL_INTERVAL
				: Long.parseLong(crawlIntervalStr);
		
		
		while (true) {
			
			try {
				fetch(nodeCrawlers);
				LOGGER.debug("Fetched all nodes, going to sleep.");
			} catch (Exception e) {
				LOGGER.error("Error while fetching nodes, going to sleep.", e);
			}
			
			try {
				Thread.sleep(crawlInterval);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
			
		}
	}

	private void fetch(List<NodeCrawler> nodeCrawlers) throws XMPPException {
		List<String> serversToCrawl = new LinkedList<String>();
		try {
			serversToCrawl = retrieveServers();
		} catch (SQLException e1) {
			LOGGER.error(e1);
		}
		
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
			
			try {
				fetchAndCrawl(discoverInfo, server, manager);
			} catch (Exception e) {
				LOGGER.warn("Could not crawls nodes from server [" + server + "]", e);
			}
			
		}
	}

	private void crawl(List<NodeCrawler> nodeCrawlers, String server,
			PubSubManager manager, Item item) {
			
		Node node = null;
		
		try {
			node = manager.getNode(item.getNode());
		} catch (Exception e) {
			LOGGER.warn("Could not read node [" + item.getNode() + "] "
					+ "from server [" + server + "]", e);
			return;
		}

		CrawlerHelper.insertNode(node, server, dataSource);

		for (NodeCrawler nodeCrawler : nodeCrawlers) {
			try {
				if (nodeCrawler.accept(node)) {
					nodeCrawler.crawl(node, server);
				}
			} catch (Exception e) {
				LOGGER.warn("Could not crawl node [" + item.getNode() + "] "
						+ "from server [" + server + "]", e);
			}
		}
	}
	
	/**
	 * @param discoverInfo
	 * @param connection 
	 * @param server 
	 * @param manager 
	 * @return
	 * @throws XMPPException 
	 */
	private void fetchAndCrawl(DiscoverItems discoverInfo, 
			String server, PubSubManager manager) throws XMPPException {
		
		int itemCount = 0;
		
		Connection connection = managers.getConnection();
		
		while (true) {
			
			Iterator<Item> itemIterator = discoverInfo.getItems();
			
			while (itemIterator.hasNext()) {
				crawl(nodeCrawlers, server, manager, itemIterator.next());
				itemCount++;
			}
			
			if (discoverInfo.getRsmSet() == null || 
					itemCount == discoverInfo.getRsmSet().getCount()) {
				break;
			}
			
			DiscoverItems request = new DiscoverItems();
			request.setTo(discoverInfo.getFrom());
			
			RSMSet rsmSet = new RSMSet();
			rsmSet.setAfter(discoverInfo.getRsmSet().getLast());
			request.setRsmSet(rsmSet);
			
			discoverInfo = (DiscoverItems) SyncPacketSend.getReply(connection, request);
		}
		
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private List<String> retrieveServers() throws SQLException {
		Statement statement = dataSource.createStatement();
		
		ResultSet resultSet = statement.executeQuery("SELECT * FROM subscribed_server");
		
		List<String> serversToCrawl = new LinkedList<String>();
		
		while (resultSet.next()) {
			serversToCrawl.add(resultSet.getString("name"));
		}
		
		ChannelDirectoryDataSource.close(statement);
		
		return serversToCrawl;
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void insertServers() throws SQLException {
		String serversToCrawlStr = configuration.getProperty("crawler.servertocrawl");
		String[] serversToCrawl = serversToCrawlStr.split(";");
		
		for (String server : serversToCrawl) {
			CrawlerHelper.insertServer(server, dataSource);
		}
		
	}

}
