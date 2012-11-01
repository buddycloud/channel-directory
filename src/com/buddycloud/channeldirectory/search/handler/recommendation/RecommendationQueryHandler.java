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
package com.buddycloud.channeldirectory.search.handler.recommendation;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Element;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.commons.solr.SolrUtils;
import com.buddycloud.channeldirectory.crawler.node.MetaDataCrawler;
import com.buddycloud.channeldirectory.search.handler.common.ChannelQueryHandler;
import com.buddycloud.channeldirectory.search.handler.common.mahout.ChannelRecommender;
import com.buddycloud.channeldirectory.search.handler.common.mahout.RecommendationResponse;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.rsm.MahoutRSMUtils;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Handles queries for user recommendation.
 * A query should contain an user jid, so
 * this handler can return recommended channels 
 * to this given user.
 *  
 */
public class RecommendationQueryHandler extends ChannelQueryHandler {

	private static final int DEFAULT_PAGE = 10;
	
	private final ChannelRecommender recommender;
	private XMPPConnection connection;
	
	public RecommendationQueryHandler(Properties properties, ChannelRecommender recommender) {
		super("http://buddycloud.com/channel_directory/recommendation_query", properties);
		this.recommender = recommender;
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element userJidElement = queryElement.element("user-jid");
		
		if (userJidElement == null) {
			return XMPPUtils.error(iq, "Query does not contain user-jid element.", 
					getLogger());
		}
		
		String userJid = userJidElement.getText();
		
		if (userJid == null || userJid.isEmpty()) {
			return XMPPUtils.error(iq, "User-jid cannot be empty.", 
					getLogger());
		}
		
		RSM rsm = RSMUtils.parseRSM(queryElement);
		rsm.setMax(rsm.getMax() != null ? rsm.getMax() : DEFAULT_PAGE);
		
		List<ChannelData> recommendedChannels;
		try {
			recommendedChannels = findRecommendedChannels(userJid, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		List<ChannelData> recommendedChannelsFullData;
		try {
			recommendedChannelsFullData = retrieveFromSolr(recommendedChannels);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		return createIQResponse(iq, recommendedChannelsFullData, rsm);
	}

	private List<ChannelData> retrieveFromSolr(List<ChannelData> recommendedChannelsData) throws Exception {
		SolrServer solrServer = SolrServerFactory.createChannelCore(getProperties());
		List<ChannelData> channelsData = new LinkedList<ChannelData>();
		for (ChannelData recommendedChannelData : recommendedChannelsData) {
			SolrQuery solrQuery = new SolrQuery("jid:" + recommendedChannelData.getId());
			QueryResponse queryResponse = solrServer.query(solrQuery);
			ChannelData channelData = convertResponse(queryResponse);
			if (channelData == null) {
				channelData = retrieveFromPubSub(recommendedChannelData.getId());
			}
			if (channelData == null) {
				channelData = recommendedChannelData;
			}
			channelsData.add(channelData);
		}
		return channelsData;
	}
	
	private ChannelData retrieveFromPubSub(String jid) {
		try {
			XMPPConnection connection = getConnection();
			Node node = getNode(jid, connection);
			if (node == null) {
				return null;
			}
			return MetaDataCrawler.fetchAndUpdateMetadata(node, jid, getProperties());
		} catch (Exception e) {
			return null;
		}
	}
	
	public XMPPConnection getConnection() throws Exception {
		if (connection == null || !connection.isConnected()) {
			connection = XMPPUtils.createCrawlerConnection(getProperties());
		}
		return connection;
	}

	private Node getNode(String jid, XMPPConnection connection)
			throws XMPPException {
		String[] splitJid = jid.split("@");
		String serverName = splitJid[1];
		ServiceDiscoveryManager discovery = new ServiceDiscoveryManager(connection);
		DiscoverItems discoverNodes = discovery.discoverItems(serverName);
		Iterator<Item> items = discoverNodes.getItems();
		
		while (items.hasNext()) {
			DiscoverItems.Item item = (DiscoverItems.Item) items.next();
			try {
				DiscoverInfo discoverInfo = discovery.discoverInfo(item.getEntityID());
				Identity identity = discoverInfo.getIdentities().next();
				if (identity.getCategory().equals("pubsub") && 
						identity.getType().equals("service")) {
					PubSubManager pubsubManager = new PubSubManager(connection, item.getEntityID());
					Node node = getNode(jid, pubsubManager);
					if (node != null) {
						return node;
					}
				}
			} catch (Exception e) {}
		}
		return null;
	}

	private Node getNode(String jid, PubSubManager pubsubManager) {
		Node node = null;
		try {
			node = pubsubManager.getNode("/user/" + jid + "/posts");	
		} catch (Exception e) {}
		
		if (node == null) {
			try {
				node = pubsubManager.getNode("/topic/" + jid + "/posts");	
			} catch (Exception e) {}
		}
		return node;
	}
	
	private static ChannelData convertResponse(QueryResponse queryResponse) {
		SolrDocumentList results = queryResponse.getResults();
		if (results.isEmpty()) {
			return null;
		}
		return SolrUtils.convertToChannelData(results.iterator().next());
	}
	
	private List<ChannelData> findRecommendedChannels(String search, RSM rsm)
			throws TasteException, SQLException {
		int howMany = MahoutRSMUtils.preprocess(rsm);
		RecommendationResponse response = recommender.recommend(search, howMany);
		return MahoutRSMUtils.postprocess(response, rsm);
	}
}
