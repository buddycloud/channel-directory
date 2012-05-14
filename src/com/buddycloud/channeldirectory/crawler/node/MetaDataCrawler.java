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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;

/**
 * Responsible for crawling {@link Node} metadata, 
 * for instance: its title, its description and 
 * its geolocation.
 *  
 */
public class MetaDataCrawler implements NodeCrawler {

	private static Logger LOGGER = Logger.getLogger(MetaDataCrawler.class);
	private static final DecimalFormat LATLNG_FORMAT = new DecimalFormat("#0.00", 
			new DecimalFormatSymbols(Locale.US));
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private Properties configuration;
	private final ChannelDirectoryDataSource dataSource;
	
	public MetaDataCrawler(Properties configuration, ChannelDirectoryDataSource dataSource) {
		this.configuration = configuration;
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#crawl(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public void crawl(Node node, String server) throws XMPPException {
		
		String nodeFullJid = node.getId();
		String nodeId = CrawlerHelper.getNodeId(nodeFullJid);
		if (nodeId == null) {
			return;
		}
		
		CrawlerHelper.enqueueNewServer(nodeId, dataSource);
		
		LeafNode leaf = (LeafNode) node;
		DiscoverInfo discoverInfo = leaf.discoverInfo();
		DataForm form = (DataForm) discoverInfo.getExtension("jabber:x:data");
		
		ChannelData channelData = new ChannelData();
		
		Geolocation geolocation = new Geolocation();
		channelData.setGeolocation(geolocation);
		channelData.setId(nodeId);

		Iterator<FormField> fields = form.getFields();
		
		while (fields.hasNext()) {
			FormField formField = fields.next();
			String fieldValue = formField.getValues().next();
			
			if (formField.getVariable().equals("pubsub#title")) {
				channelData.setTitle(fieldValue);
			} else if (formField.getVariable().equals("pubsub#description")) {
				channelData.setDescription(fieldValue);
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-lat")) {
				geolocation.setLat(Double.valueOf(fieldValue));
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-lon")) {
				geolocation.setLng(Double.valueOf(fieldValue));
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-text")) {
				geolocation.setText(fieldValue);
			} else if (formField.getVariable().equals("pubsub#creation_date")) {
				try {
					channelData.setCreationDate(DATE_FORMAT.parse(fieldValue));
				} catch (ParseException e) {
					LOGGER.warn("Unable to parse creation date for [" + nodeId + "].", e);
				}
			} else if (formField.getVariable().equals("buddycloud#channel_type")) {
				channelData.setChannelType(fieldValue);
			}
		}
		
		if (geolocation.getLat() == null && geolocation.getLng() == null
				&& geolocation.getText() == null) {
			channelData.setGeolocation(null);
		}
		
		try {
			updateSubscribedNode(nodeId, server);
		} catch (SQLException e1) {
			LOGGER.warn("Could not update subscribed node.", e1);
		}
		
		try {
			insertOrUpate(channelData);
		} catch (Exception e) {
			LOGGER.warn("Could not update node metadata.", e);
		}
		
	}

	private void insertOrUpate(ChannelData channelData) throws Exception {

		SolrInputDocument object = new SolrInputDocument();
		
		if (channelData.getGeolocation() != null) {
			Double lat = channelData.getGeolocation().getLat();
			Double lng = channelData.getGeolocation().getLng();
			
			if (lat != null && lng != null) {
				object.setField("geoloc", LATLNG_FORMAT.format(lat) + "," + LATLNG_FORMAT.format(lng));
			}
			String geoText = channelData.getGeolocation().getText();
			if (geoText != null) {
				object.setField("geoloc-text", geoText);
			}
		}
		
		object.setField("jid", channelData.getId());
		object.setField("title", channelData.getTitle());
		object.setField("description", channelData.getDescription());
		object.setField("creation-date", channelData.getCreationDate());
		object.setField("channel-type", channelData.getChannelType()); //topic or personal
		
		SolrServer solrServer = SolrServerFactory.createChannelCore(configuration);
		solrServer.add(object);
		solrServer.commit();
	}

	private void updateSubscribedNode(String nodeName, String server) throws SQLException {
		PreparedStatement prepareStatement = dataSource.prepareStatement(
				"UPDATE subscribed_node SET metadata_updated = ? WHERE name = ? AND server = ?", 
				new Date(System.currentTimeMillis()), nodeName, server);
		prepareStatement.execute();
		ChannelDirectoryDataSource.close(prepareStatement);
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#accept(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public boolean accept(Node node) {
		return node.getId().endsWith("/posts");
	}

}
