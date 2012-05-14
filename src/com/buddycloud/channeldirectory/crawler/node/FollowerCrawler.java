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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.RSMSet;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.AffiliationsExtension;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

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
		
		Object affiliationsProvider = ProviderManager.getInstance().getExtensionProvider(
				PubSubElementType.AFFILIATIONS.getElementName(), PubSubNamespace.BASIC.getXmlns());
		ProviderManager.getInstance().addExtensionProvider(PubSubElementType.AFFILIATIONS.getElementName(), 
				PubSubNamespace.OWNER.getXmlns(), affiliationsProvider);
		
		Object affiliationProvider = ProviderManager.getInstance().getExtensionProvider(
				"affiliation", PubSubNamespace.BASIC.getXmlns());
		ProviderManager.getInstance().addExtensionProvider("affiliation", 
				PubSubNamespace.OWNER.getXmlns(), affiliationProvider);
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#crawl(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public void crawl(Node node, String server) throws Exception {
		List<Affiliation> affiliations = getAffiliations(node);
		
		String item = node.getId();
		String itemJID = CrawlerHelper.getNodeId(item);
		
		LOGGER.debug("Fetching followers for " + itemJID);
		
		if (itemJID == null) {
			return;
		}
		
		for (Affiliation affiliation : affiliations) {
		
			String user = affiliation.getNodeId();
			
			CrawlerHelper.enqueueNewServer(user, dataSource);
			
			Long userId = fetchRowId(user, "t_user");
			Long itemId = fetchRowId(itemJID, "item");
			
			Statement selectTasteSt = dataSource.createStatement();
			ResultSet selectTasteResult = selectTasteSt.executeQuery(
					"SELECT * FROM taste_preferences WHERE user_id = '" + userId + "' " +
					"AND item_id = '" + itemId + "'");
			
			boolean affiliationExists = selectTasteResult.next();
			
			LOGGER.debug(user + " follows " + itemJID + 
					". Affiliation exists? " + affiliationExists);
			
			if (!affiliationExists) {
				Statement insertTasteSt = dataSource.createStatement();
				insertTasteSt.execute("INSERT INTO taste_preferences(user_id, item_id) " +
						"VALUES ('" + userId + "', '" + itemId + "')");
				ChannelDirectoryDataSource.close(insertTasteSt);
			}
			
			ChannelDirectoryDataSource.close(selectTasteSt);
		}
	
		try {
			updateSubscribedNode(node.getId(), server);
		} catch (SQLException e1) {
			LOGGER.warn("Could not update subscribed node", e1);
		}
		
	}

	private void updateSubscribedNode(String nodeName, String server) throws SQLException {
		
		PreparedStatement prepareStatement = dataSource.prepareStatement(
				"UPDATE subscribed_node SET subscribers_updated = ? WHERE name = ? AND server = ?", 
				new Date(System.currentTimeMillis()), nodeName, server);
		prepareStatement.execute();
		ChannelDirectoryDataSource.close(prepareStatement);
	}
	
	private Long fetchRowId(String user, String tableName)
			throws SQLException {
		
		Statement selectRowSt = dataSource.createStatement();
		ResultSet selectRowResult = selectRowSt.executeQuery(
				"SELECT id FROM " + tableName + " WHERE jid = '" + user + "'");
		Long userId = null;
		
		if (selectRowResult.next()) {
			userId = selectRowResult.getLong("id");
		} else {
			Statement insertRowSt = dataSource.createStatement();
			insertRowSt.execute("INSERT INTO " + tableName + "(jid) VALUES ('" + user + "') RETURNING id");
			
			ResultSet insertUserResult = insertRowSt.getResultSet();
			insertUserResult.next();
			userId = insertUserResult.getLong("id");
			
			ChannelDirectoryDataSource.close(insertRowSt);
		}
		
		ChannelDirectoryDataSource.close(selectRowSt);
		
		return userId;
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#accept(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public boolean accept(Node node) {
		return node.getId().endsWith("/posts");
	}
	
	private List<Affiliation> getAffiliations(Node node) throws XMPPException {
		
		List<Affiliation> affiliations = new LinkedList<Affiliation>();
		
		PubSub request = node.createPubsubPacket(Type.GET, 
				new NodeExtension(PubSubElementType.AFFILIATIONS, node.getId()), 
				PubSubNamespace.OWNER);
		
		while (true) {
			
			PubSub reply = (PubSub) node.sendPubsubPacket(Type.GET, request);
			
			AffiliationsExtension subElem = (AffiliationsExtension) reply.getExtension(
					PubSubElementType.AFFILIATIONS.getElementName(), PubSubNamespace.BASIC.getXmlns());
			
			affiliations.addAll(subElem.getAffiliations());
			
			if (reply.getRsmSet() == null || 
					affiliations.size() == reply.getRsmSet().getCount()) {
				break;
			}
			
			RSMSet rsmSet = new RSMSet();
			rsmSet.setAfter(reply.getRsmSet().getLast());
			request.setRsmSet(rsmSet);
			
		}
		
		return affiliations;
	}
	
}
