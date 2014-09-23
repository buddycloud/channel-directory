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
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.PacketUtil;
import org.jivesoftware.smackx.pubsub.BuddycloudAffiliation;
import org.jivesoftware.smackx.pubsub.BuddycloudNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.rsm.packet.RSMSet;

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
	public void crawl(BuddycloudNode node, String server) throws Exception {
		List<BuddycloudAffiliation> affiliations = getAffiliations(node);
		
		String item = node.getId();
		String itemJID = CrawlerHelper.getNodeId(item);
		
		LOGGER.debug("Fetching followers for " + itemJID);
		
		if (itemJID == null) {
			return;
		}
		
		for (BuddycloudAffiliation affiliation : affiliations) {
			try {
				processAffiliation(itemJID, affiliation);
			} catch (Exception e) {
				LOGGER.warn(e);
			}
		}
	
		try {
			updateSubscribedNode(node.getId(), server);
		} catch (SQLException e1) {
			LOGGER.warn("Could not update subscribed node", e1);
		}
		
	}

	private void processAffiliation(String itemJID, BuddycloudAffiliation affiliation)
			throws SQLException {
		String user = affiliation.getNodeId();
		
		CrawlerHelper.enqueueNewServer(user, dataSource);
		
		Long userId = fetchRowId(user, "t_user");
		Long itemId = fetchRowId(itemJID, "item");
		
		boolean affiliationExists = affiliationExists(userId, itemId);
		
		LOGGER.debug(user + " follows " + itemJID + 
				". Affiliation exists? " + affiliationExists);
		
		if (!affiliationExists) {
			insertTaste(userId, itemId);
		}
	}

	private boolean affiliationExists(Long userId, Long itemId)
			throws SQLException {
		Statement selectTasteSt = null;
		try {
			selectTasteSt = dataSource.createStatement();
			ResultSet selectTasteResult = selectTasteSt.executeQuery(
					"SELECT * FROM taste_preferences WHERE user_id = '" + userId + "' " +
							"AND item_id = '" + itemId + "'");
			return selectTasteResult.next();
		} catch (SQLException e) {
			LOGGER.error(e);
			throw e;
		} finally {
			ChannelDirectoryDataSource.close(selectTasteSt);
		}
	}

	private void insertTaste(Long userId, Long itemId) throws SQLException {
		Statement insertTasteSt = null;
		try {
			insertTasteSt = dataSource.createStatement();
			insertTasteSt.execute("INSERT INTO taste_preferences(user_id, item_id) " +
					"VALUES ('" + userId + "', '" + itemId + "')");
		} catch (SQLException e) {
			LOGGER.error(e);
			throw e;
		} finally {
			ChannelDirectoryDataSource.close(insertTasteSt);
		}
	}

	private void updateSubscribedNode(String nodeName, String server) throws SQLException {
		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = dataSource.prepareStatement(
					"UPDATE subscribed_node SET subscribers_updated = ? WHERE name = ? AND server = ?", 
					new Date(System.currentTimeMillis()), nodeName, server);
			prepareStatement.execute();
		} catch (SQLException e) {
			LOGGER.error(e);
			throw e;
		} finally {
			ChannelDirectoryDataSource.close(prepareStatement);
		}
	}
	
	private Long fetchRowId(String user, String tableName) throws SQLException {
		Statement selectRowSt =null;
		try {
			selectRowSt = dataSource.createStatement();
			ResultSet selectRowResult = selectRowSt.executeQuery(
					"SELECT id FROM " + tableName + " WHERE jid = '" + user + "'");
			Long userId = null;
			
			if (selectRowResult.next()) {
				userId = selectRowResult.getLong("id");
			} else {
				userId = insertTasteObject(user, tableName);
			}
			return userId;
		} catch (SQLException e) {
			LOGGER.error(e);
			throw e;
		} finally {
			ChannelDirectoryDataSource.close(selectRowSt);
		}
	}

	private Long insertTasteObject(String object, String tableName) throws SQLException {
		Statement insertRowSt = null;
		try {
			insertRowSt = dataSource.createStatement();
			insertRowSt.execute("INSERT INTO " + tableName + "(jid) VALUES ('" + object + "') RETURNING id");
			ResultSet insertUserResult = insertRowSt.getResultSet();
			insertUserResult.next();
			return insertUserResult.getLong("id");
		} catch (SQLException e) {
			LOGGER.error(e);
			throw e;
		} finally {
			ChannelDirectoryDataSource.close(insertRowSt);
		}
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#accept(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public boolean accept(BuddycloudNode node) {
		return node.getId().endsWith("/posts");
	}
	
	private List<BuddycloudAffiliation> getAffiliations(BuddycloudNode node) throws XMPPException {
		
		List<BuddycloudAffiliation> affiliations = new LinkedList<BuddycloudAffiliation>();
		
		RSMSet nextRsmSet = null;
		
		while (true) {
			
			List<PacketExtension> additionalExtensions = new LinkedList<PacketExtension>();
			List<PacketExtension> returnedExtensions = new LinkedList<PacketExtension>();
			if (nextRsmSet != null) {
				additionalExtensions.add(nextRsmSet);
			}
			
			List<BuddycloudAffiliation> nodeAffiliations = null;
			try {
				nodeAffiliations = node.getBuddycloudAffiliations(
						additionalExtensions, returnedExtensions);
			} catch (Exception e) {
				break;
			}
			
			nodeAffiliations.addAll(nodeAffiliations);
			
			RSMSet returnedRsmSet = PacketUtil.packetExtensionfromCollection(
					returnedExtensions, RSMSet.ELEMENT, RSMSet.NAMESPACE);
			
			if (returnedRsmSet == null || 
					nodeAffiliations.size() == returnedRsmSet.getCount()) {
				break;
			}
			
			nextRsmSet = RSMSet.newAfter(returnedRsmSet.getLast());
		}
		
		return affiliations;
	}
}
