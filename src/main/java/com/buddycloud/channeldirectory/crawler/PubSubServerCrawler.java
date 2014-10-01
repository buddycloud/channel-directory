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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.BuddycloudNode;
import org.jivesoftware.smackx.pubsub.BuddycloudPubsubManager;
import org.jivesoftware.smackx.rsm.packet.RSMSet;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.crawler.node.CrawlerHelper;
import com.buddycloud.channeldirectory.crawler.node.DiscoveryUtils;
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
	
	private static final int RECONNECTION_INTERVAL = 30000;
	private static final int RECONNECTION_RETRIES = 5;
	private static long DEF_CRAWL_INTERVAL = 60000 * 30; // 30 minutes
	
	private final Properties configuration;
	private final PubSubManagers managers;
	private final ChannelDirectoryDataSource dataSource;
	
	private List<NodeCrawler> nodeCrawlers;
	private final XMPPConnection connection;
	
	public PubSubServerCrawler(Properties configuration, PubSubManagers managers, 
			ChannelDirectoryDataSource dataSource, XMPPConnection connection, 
			PubSubSubscriptionListener listener) {
		this.configuration = configuration;
		this.managers = managers;
		this.dataSource = dataSource;
		this.connection = connection;
	}
	
	public void start() {
		
		this.nodeCrawlers = new LinkedList<NodeCrawler>();
		nodeCrawlers.add(new MetaDataCrawler(configuration, dataSource));
		nodeCrawlers.add(new PostCrawler(configuration, dataSource));
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
		List<String> domainsToCrawl = new LinkedList<String>();
		try {
			domainsToCrawl = retrieveServers();
		} catch (SQLException e1) {
			LOGGER.error(e1);
		}
		
		for (String domain : domainsToCrawl) {
			LOGGER.debug("Discovering channel server on " + domain);
			String channelServer = DiscoveryUtils.discoverChannelServer(connection, domain);
			if (channelServer != null) {
				crawlChannelServer(channelServer);
			}
		}
	}

	private void crawlChannelServer(String channelServer) {
		LOGGER.debug("Discovering nodes on " + channelServer);
		waitForReconnection();
		
		BuddycloudPubsubManager manager = managers.getPubSubManager(channelServer);
		
		// Crawling firehose
		LOGGER.debug("Crawling firehose node on " + channelServer);
		try {
			crawl(nodeCrawlers, channelServer, manager.getFirehoseNode());
		} catch (Exception e) {
			LOGGER.warn("Could not crawl firehose node on [" + channelServer + "]. " +
					"Falling back to a per-node discovery.", e);
		}
		
		DiscoverItems discoverInfo = null;
		try {
			discoverInfo = manager.discoverNodes(null);
		} catch (Exception e) {
			LOGGER.warn("Could not fetch nodes from server [" + channelServer + "]", e);
			return;
		}
		if (discoverInfo == null) {
			return;
		}
		
		LOGGER.debug("Crawling items on " + channelServer);
		try {
			fetchAndCrawl(discoverInfo, channelServer, manager);
		} catch (Exception e) {
			LOGGER.warn("Could not crawls nodes from server [" + channelServer + "]", e);
		}
	}

	private void waitForReconnection() {
		int retries = RECONNECTION_RETRIES;
		while (--retries > 0) {
			if (connection.isConnected()) {
				break;
			}
			try {
				Thread.sleep(RECONNECTION_INTERVAL);
			} catch (InterruptedException e) {}
		}
	}

	private void crawl(List<NodeCrawler> nodeCrawlers, String server,
			BuddycloudPubsubManager manager, DiscoverItems.Item nodeItem) {
		
		waitForReconnection();
		
		BuddycloudNode node = null;
		
		try {
			node = manager.getNode(nodeItem.getNode());
		} catch (Exception e) {
			LOGGER.warn("Could not read node [" + nodeItem.getNode() + "] "
					+ "from server [" + server + "]", e);
			return;
		}

		crawl(nodeCrawlers, server, node);
	}

	private void crawl(List<NodeCrawler> nodeCrawlers, String server,
			BuddycloudNode node) {
		CrawlerHelper.insertNode(node, server, dataSource);

		for (NodeCrawler nodeCrawler : nodeCrawlers) {
			try {
				if (nodeCrawler.accept(node)) {
					nodeCrawler.crawl(node, server);
				}
			} catch (Exception e) {
				LOGGER.warn("Could not crawl node [" + node.getId() + "] "
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
			String server, BuddycloudPubsubManager manager) throws XMPPException {
		
		int itemCount = 0;
		
		XMPPConnection connection = managers.getConnection();
		
		while (true) {
			
			List<DiscoverItems.Item> serverItems = discoverInfo.getItems();
			
			for (DiscoverItems.Item item : serverItems) {
				crawl(nodeCrawlers, server, manager, item);
				itemCount++;
			}
			
			RSMSet rsmSet = (RSMSet) discoverInfo.getExtension(RSMSet.NAMESPACE);
			
			if (rsmSet == null || 
					itemCount == rsmSet.getCount()) {
				break;
			}
			
			DiscoverItems request = new DiscoverItems();
			request.setTo(discoverInfo.getFrom());
			
			RSMSet nexRsmSet = RSMSet.newAfter(rsmSet.getLast());
			request.addExtension(nexRsmSet);
			
			try {
				discoverInfo = (DiscoverItems) connection
						.createPacketCollectorAndSend(request).nextResultOrThrow();
			} catch (Exception e) {
				break;
			}
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
