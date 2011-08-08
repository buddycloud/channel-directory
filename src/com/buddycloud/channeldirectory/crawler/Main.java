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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import com.buddycloud.channeldirectory.crawler.node.FollowerCrawler;
import com.buddycloud.channeldirectory.crawler.node.MetaDataCrawler;
import com.buddycloud.channeldirectory.crawler.node.NodeCrawler;
import com.buddycloud.channeldirectory.crawler.node.PostCrawler;

/**
 * Creates and starts the Crawler component.
 * 
 */
public class Main {

	private static final String CONFIGURATION_FILE = System.getenv("CHANNEL_DIRECTORY_HOME") + "/configuration.properties";
	private static Logger LOGGER = Logger.getLogger(Main.class);

	/**
	 * Starts the crawler. This methods create several {@link NodeCrawler}
	 * for each crawling subject.
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws XMPPException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws Exception {
		
		Properties configuration = loadConfiguration();
		XMPPConnection connection = createConnection(configuration);
		
		String userId = connection.getUser();
		
		addTraceListeners(connection);
		
		String serversToCrawlStr = configuration.getProperty("crawler.servertocrawl");
		String[] serversToCrawl = serversToCrawlStr.split(",");
		
		List<NodeCrawler> nodeCrawlers = new LinkedList<NodeCrawler>();
		nodeCrawlers.add(new MetaDataCrawler(configuration));
		nodeCrawlers.add(new PostCrawler(configuration));
		nodeCrawlers.add(new FollowerCrawler(configuration));
		
		while (true) {
			
			for (String server : serversToCrawl) {
				PubSubManager manager = new PubSubManager(connection, server);
				DiscoverItems discoverInfo = null;
				try {
					discoverInfo = manager.discoverNodes(null);
				} catch (Exception e) {
					LOGGER.warn("Could not fetch nodes from server [" + server + "]", e);
				}
				
				if (discoverInfo == null) {
					//TODO To be removed
					crawlBeerChannel(nodeCrawlers, manager);
					continue;
				}
				
				Iterator<Item> idsIterator = discoverInfo.getItems();
				
				while (idsIterator.hasNext()) {
					Item item = idsIterator.next();
					for (NodeCrawler nodeCrawler : nodeCrawlers) {
						try {
							Node node = manager.getNode(item.getName());
							node.subscribe(userId);
							nodeCrawler.crawl(node);
						} catch (Exception e) {
							LOGGER.warn("Could not crawl node [" + item.getName() + "] " +
									"from server [" + server + "]", e);
						}
					}
				}
			}
			
			Thread.sleep(60000 * 5);
		}
	}

	private static void crawlBeerChannel(List<NodeCrawler> nodeCrawlers,
			PubSubManager manager) throws XMPPException {
		Node node = manager.getNode("/channel/beer");
		for (NodeCrawler nodeCrawler : nodeCrawlers) {
			try {
				nodeCrawler.crawl(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void addTraceListeners(XMPPConnection connection) {
		PacketFilter iqFilter = new PacketFilter() {
			@Override
			public boolean accept(Packet arg0) {
				return arg0 instanceof IQ;
			}
		};

		connection.addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Packet arg0) {
				LOGGER.debug("S: " + arg0.toXML());
			}
		}, iqFilter);

		connection.addPacketListener(new PacketListener() {

			@Override
			public void processPacket(Packet arg0) {
				LOGGER.debug("R: " + arg0.toXML());
			}
		}, iqFilter);
	}

	private static XMPPConnection createConnection(Properties configuration)
			throws XMPPException {
		
		String serverName = configuration.getProperty("crawler.xmpp.servername");
		String userName = configuration.getProperty("crawler.xmpp.username");
		
		ConnectionConfiguration cc = new ConnectionConfiguration(
				serverName,
				Integer.parseInt(configuration.getProperty("crawler.xmpp.port")));
		
		XMPPConnection connection = new XMPPConnection(cc);
		connection.connect();
		connection.login(userName, configuration.getProperty("crawler.xmpp.password"));
		
		return connection;
	}

	private static Properties loadConfiguration() throws IOException {
		Properties configuration = new Properties();
		try {
			configuration.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			LOGGER.fatal("Configuration could not be loaded.", e);
			throw e;
		}
		return configuration;
	}

}
