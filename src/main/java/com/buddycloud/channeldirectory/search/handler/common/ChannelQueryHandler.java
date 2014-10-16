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
package com.buddycloud.channeldirectory.search.handler.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Element;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.pubsub.BuddycloudNode;
import org.jivesoftware.smackx.pubsub.BuddycloudPubsubManager;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.commons.solr.SolrUtils;
import com.buddycloud.channeldirectory.crawler.node.MetaDataCrawler;
import com.buddycloud.channeldirectory.search.handler.AbstractQueryHandler;
import com.buddycloud.channeldirectory.search.handler.QueryHandler;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.utils.FeatureUtils;
import com.buddycloud.channeldirectory.search.utils.GeolocationUtils;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Abstract class for {@link QueryHandler} that returns
 * ChannelData.
 * 
 */
public abstract class ChannelQueryHandler extends AbstractQueryHandler {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");
	private XMPPConnection connection;
	
	public ChannelQueryHandler(String namespace, Properties properties) {
		super(namespace, properties);
	}

	protected IQ createIQResponse(IQ iq, List<ChannelData> allContent, RSM rsm) {
		IQ result = IQ.createResultIQ(iq);
		
		Element queryEl = iq.getElement().element("query");
		Set<String> options = FeatureUtils.parseOptions(queryEl);
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (ChannelData channelObject : allContent) {
			Element itemElement = queryElement.addElement("item");
			
			FeatureUtils.addAttribute(options, itemElement, "jid",
					channelObject.getId());
			FeatureUtils.addAttribute(options, itemElement, "type",
					channelObject.getType());
			FeatureUtils.addAttribute(options, itemElement, "description",
					channelObject.getDescription());
			
			if (channelObject.getCreationDate() != null) {
				FeatureUtils.addAttribute(options, itemElement, "created",
						DATE_FORMAT.format(channelObject.getCreationDate()));
			}
			
			FeatureUtils.addElement(options, itemElement, "title",
					channelObject.getTitle());
			FeatureUtils.addElement(options, itemElement, "channel_type", 
					channelObject.getChannelType());
			FeatureUtils.addElement(options, itemElement, "default_affiliation", 
					channelObject.getDefaultAffiliation());
			
			if (channelObject.getGeolocation() != null) {
				Element geoElement = FeatureUtils.addNamespaceElement(
						options, itemElement, "geoloc", Geolocation.NAMESPACE);
				GeolocationUtils.appendGeoLocation(geoElement, channelObject.getGeolocation());
			}
			
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}
	
	protected List<ChannelData> retrieveFromSolr(List<ChannelData> recommendedChannelsData) throws Exception {
		SolrServer solrServer = new SolrServerFactory().createChannelCore(getProperties());
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
	
	private static ChannelData convertResponse(QueryResponse queryResponse) {
		SolrDocumentList results = queryResponse.getResults();
		if (results.isEmpty()) {
			return null;
		}
		return SolrUtils.convertToChannelData(results.iterator().next());
	}
	
	private ChannelData retrieveFromPubSub(String jid) {
		try {
			XMPPConnection connection = getConnection();
			BuddycloudNode node = getNode(jid, connection);
			if (node == null) {
				return null;
			}
			return MetaDataCrawler.fetchAndUpdateMetadata(node, jid, getProperties());
		} catch (Exception e) {
			return null;
		}
	}
	
	private XMPPConnection getConnection() throws Exception {
		if (connection == null || !connection.isConnected()) {
			connection = XMPPUtils.createCrawlerConnection(getProperties());
		}
		return connection;
	}
	
	private BuddycloudNode getNode(String jid, XMPPConnection connection)
			throws Exception {
		String[] splitJid = jid.split("@");
		String serverName = splitJid[1];
		ServiceDiscoveryManager discovery = ServiceDiscoveryManager.getInstanceFor(connection);
		DiscoverItems discoverNodes = discovery.discoverItems(serverName);
		List<DiscoverItems.Item> items = discoverNodes.getItems();
		
		for (Item item : items) {
			try {
				DiscoverInfo discoverInfo = discovery.discoverInfo(item.getEntityID());
				Identity identity = discoverInfo.getIdentities().get(0);
				if (identity.getCategory().equals("pubsub") && 
						identity.getType().equals("service")) {
					BuddycloudPubsubManager pubsubManager = new BuddycloudPubsubManager(
							connection, item.getEntityID());
					BuddycloudNode node = getNode(jid, pubsubManager);
					if (node != null) {
						return node;
					}
				}
			} catch (Exception e) {}
		}
		return null;
	}
	
	private BuddycloudNode getNode(String jid, BuddycloudPubsubManager pubsubManager) {
		BuddycloudNode node = null;
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
}
