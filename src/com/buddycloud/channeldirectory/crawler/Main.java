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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import com.buddycloud.channeldirectory.crawler.node.NodeCrawler;

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
		addTraceListeners(connection);
		
		PubSubManagers managers = new PubSubManagers(connection);
		PubSubSubscriptionListener listener = new PubSubSubscriptionListener(configuration, managers);
		listener.start();
		
		new PubSubServerCrawler(configuration, managers, listener).start();
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
